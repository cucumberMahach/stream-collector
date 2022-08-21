package com.streamcollector.app.service;

import com.streamcollector.app.database.entities.*;
import com.streamcollector.app.database.utils.PlatformScope;
import com.streamcollector.app.database.DatabaseUtil;
import com.streamcollector.app.grabber.UserType;
import com.streamcollector.app.remover.Remover;
import com.streamcollector.app.util.DataUtil;
import com.streamcollector.app.util.FutureUtils;
import com.streamcollector.app.util.Pair;
import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.logging.LogStatus;
import org.hibernate.StatelessSession;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.grabber.GrabChannelResult;
import com.streamcollector.app.grabber.Grabber;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CircleService extends AbstractService {
    private final String LAST_GRABS_FILENAME = "lastGrabs.json";

    private int toNextCircleMs = 1000 * 20;
    private int noChannelsWaitMs = 1000 * 10;
    private int allChannelsErrorsWaitMs = 1000 * 5;
    private int noUpdatesWaitMs = 1000 * 20;

    private int viewerLeaveSec = 5 * 60;
    private int lastGrabActualDurationSec = 10 * 60;

    private ZonedDateTime circleStartTime;
    private static final int threadsToUse = Math.max((int) (Runtime.getRuntime().availableProcessors() * 1.5f), 10);
    private static final ExecutorService pool = Executors.newFixedThreadPool(threadsToUse);
    private List<GrabChannelResult> lastGrabResult = new ArrayList<>();
    private PlatformScope platformScope;
    private Remover remover;

    public CircleService() {
        super("circle");
    }

    private StatelessSession getSession(){
        var settings = Settings.instance.getSettings();
        return DatabaseUtil.getStateLessSession(settings.circlesDatabase);
    }

    private void loadLastGrabs(){
        if (Files.exists(Paths.get(LAST_GRABS_FILENAME))){
            try {
                lastGrabResult = GrabChannelResult.deserializeGrabsFromFile(LAST_GRABS_FILENAME);
                int loadedSize = lastGrabResult.size();
                var nowTime = TimeUtil.getZonedNow();
                lastGrabResult = lastGrabResult.stream().filter(result -> nowTime.minusSeconds(lastGrabActualDurationSec).isBefore(result.timestamp)).collect(Collectors.toList());
                lastGrabResult.forEach(result -> result.chattersGlobal.chatters.fillSetsAndConstructMaps());
                writeLog(LogStatus.Success, String.format("Подгружены lastGrabs с диска - %d записей, актуальных - %d", loadedSize, lastGrabResult.size()));
            } catch (Throwable e) {
                writeLog(LogStatus.Error, "Ошибка при загрузке lastGrabs с диска: " + DataUtil.getStackTrace(e));
                e.printStackTrace();
            }
        }else{
            writeLog(LogStatus.Warning, "LastGrabs отсутствуют на диске");
        }
    }

    private void saveLastGrabs(){
        try {
            GrabChannelResult.serializeGrabsToFile(lastGrabResult, LAST_GRABS_FILENAME);
            writeLog(LogStatus.Success, "LastGrabs сохранены на диск");
        } catch (Throwable e) {
            writeLog(LogStatus.Error, "Ошибка при сохранении lastGrabs на диск: " + DataUtil.getStackTrace(e));
            e.printStackTrace();
        }
    }

    @Override
    protected void work() {
        loadLastGrabs();
        StatelessSession session = null;
        remover = new Remover(this);
        try {
            while (true) {
                if (!running)
                    break;

                writeLog(LogStatus.None, "======================================== Новый цикл ========================================");
                session = getSession();

                Grabber grabber = new Grabber();
                grabber.setServiceToLog(this);

                var query = session.createNativeQuery("select * from `twitch-collector`.channelstocheck join `twitch-collector`.sites on(channelstocheck.site_id = sites.id) ORDER BY priority", ChannelToCheckEntity.class);
                var channels = query.list();

                if (channels.isEmpty()) {
                    writeLog(LogStatus.Warning, "В БД нет каналов для сбора информации");
                    sleep(noChannelsWaitMs);
                    continue;
                }

                for (var channel : channels) {
                    grabber.getChannelsToGrab().add(new Pair<>(Platform.fromNameInDB(channel.site.site), channel.name));
                }
                circleStartTime = TimeUtil.getZonedNow();

                session.close();

                long startGrabTime = System.currentTimeMillis();
                grabber.startGrabAsyncHttp();
                int grabDuration = (int)(System.currentTimeMillis() - startGrabTime);
                writeLog(LogStatus.Success, String.format("Сбор данных занял %d мс", grabDuration));

                var grabbedChannels = grabber.getResults();

                List<GrabChannelResult> grabsWithoutErrors = new ArrayList<>();

                int errorsCount = 0;
                for (var grabCh : grabbedChannels) {
                    if (grabCh.isError()) {
                        errorsCount++;
                        writeLog(LogStatus.Error, "Ошибка при сборе информации о канале " + grabCh.channelName + ": " + grabCh.getError());
                    }else{
                        grabsWithoutErrors.add(grabCh);
                    }
                }
                if (errorsCount == grabbedChannels.size()) {
                    writeLog(LogStatus.Error, "При сборе информации во всех каналах возникли ошибки");
                    sleep(allChannelsErrorsWaitMs);
                    continue;
                }

                // Проверка, обновились ли данные

                var remainOlds = new ArrayList<GrabChannelResult>();
                var updatedGrabs = isGrabsUpdated(lastGrabResult, grabsWithoutErrors, remainOlds);

                if (updatedGrabs.isEmpty()) {
                    writeLog(LogStatus.Success, "Обновлений данных не обнаружено");
                    sleep(noUpdatesWaitMs);
                    continue;
                }

                session = getSession();

                writeLog(LogStatus.Success, String.format("Обнаружено обновлений данных %d из %d каналов", updatedGrabs.size(), grabsWithoutErrors.size()));

                final float calculatedDuration = updatedGrabs.size() * getAvgTimePerChannelSec(session, 5);

                platformScope = new PlatformScope();
                platformScope.load(session);

                var userTypes = getUsersTypes(session);

                Pair<CircleEntity, CircleEntity> currentAndLastCircles = doCurrentCircle(session, grabDuration);
                var currentCircle = currentAndLastCircles.first;
                var lastCircle = currentAndLastCircles.second;

                for (var grabCh : updatedGrabs) {
                    writeLog(LogStatus.None, "Запись канала " + grabCh.channelName);

                    var currentChannelPair = doCurrentChannel(session, currentCircle, grabCh);
                    var currentChannel = currentChannelPair.first;

                    //Channel
                    if (currentChannelPair.second) {
                        session.insert(currentChannel);
                    } else {
                        session.update(currentChannel);
                    }

                    ChannelCircleEntity lastChannelCircle = null;
                    if (lastCircle != null && !currentChannelPair.second) {
                        lastChannelCircle = getLastChannelCircle(session, lastCircle, currentChannel);
                    }

                    var channelCircle = doChannelCircle(currentCircle, currentChannel, grabCh);

                    //Channel Circle
                    session.insert(channelCircle);

                    var startTime = System.currentTimeMillis();
                    var usersStatistic = doUsers(session, grabCh);

                    writeLog(LogStatus.Success, String.format("Пользователи записаны (inserts - %d, updates - %d, all - %d): %d мс", usersStatistic.first, usersStatistic.second, usersStatistic.first + usersStatistic.second, System.currentTimeMillis() - startTime));


                    startTime = System.currentTimeMillis();
                    var usersChannels = doUsersChannels(session, currentChannel, currentCircle, lastChannelCircle, userTypes, grabCh, calculatedDuration);
                    writeLog(LogStatus.Success, String.format("Подготовка зрителей к записи: %d мс", System.currentTimeMillis() - startTime));

                    //================================FINAL TRANSACTION================================




                    //Users Channels
                    Pair<Long, Long> stats = new Pair<>(0L, 0L);
                    for (var r : usersChannels){
                        stats.first += r.first;
                        stats.second += r.second;
                    }
                    /*startTime = System.currentTimeMillis();
                    int usersInserts = 0, usersUpdate = 0;
                    for (var r : usersChannels) {
                        if (r != null) {
                            for (var user : r.first) {
                                session.update(user);
                                usersUpdate++;
                            }
                            for (var user : r.second) {
                                session.insert(user);
                                usersInserts++;
                            }
                        }
                    }*/
                    writeLog(LogStatus.Success, String.format("Запись зрителей (inserts - %d, updates - %d, all - %d): %d мс", stats.first, stats.second, stats.first + stats.second, System.currentTimeMillis() - startTime));

                    //Current Circle update
                    currentCircle.totalChannels++;
                    session.beginTransaction();
                    session.update(currentCircle);
                    session.getTransaction().commit();
                    //=================================================================================
                }

                // Удаление старых grabs и добавление новых, сброс на диск
                if (!lastGrabResult.isEmpty()) {
                    for (var grab : updatedGrabs){
                        if (grab.previous != null && lastGrabResult.contains(grab.previous)){
                            lastGrabResult.remove(grab.previous);
                            lastGrabResult.add(grab);
                        }else{
                            lastGrabResult.add(grab);
                        }
                    }
                }else{
                    lastGrabResult.addAll(updatedGrabs);
                }
                saveLastGrabs();

                session.beginTransaction();
                currentCircle.endTime = TimeUtil.getZonedNow();
                session.update(currentCircle);
                session.getTransaction().commit();

                writeLog(LogStatus.Success, String.format("Обработано каналов: %d (%s)", currentCircle.totalChannels, TimeUtil.formatDurationHoursMs(Duration.between(currentCircle.startTime, currentCircle.endTime))));
                remover.check(session);
                session.close();
                sleep(toNextCircleMs);
            }
        } catch (Exception e) {
            if (session != null)
                session.close();
            if (!(e instanceof InterruptedException))
                writeLog(LogStatus.Error, "Исключение в сервисе: " + e.getMessage() + " " + DataUtil.getStackTrace(e));
        }
    }
    private List<GrabChannelResult> isGrabsUpdated(List<GrabChannelResult> oldG, List<GrabChannelResult> newG, List<GrabChannelResult> outRemainOlds){
        List<GrabChannelResult> newGrabs = new ArrayList<>(newG);
        List<GrabChannelResult> oldGrabs = new ArrayList<>(oldG);
        List<GrabChannelResult> updated = new ArrayList<>();

        if (oldGrabs.isEmpty() && !newGrabs.isEmpty()){
            return newGrabs;
        }



        Iterator<GrabChannelResult> i = newGrabs.iterator();
        while (i.hasNext()) {
            var newGrab = i.next();
            Iterator<GrabChannelResult> j = oldGrabs.iterator();
            while (j.hasNext()) {
                var oldGrab = j.next();
                if (newGrab.channelName.equals(oldGrab.channelName) && newGrab.platform == oldGrab.platform){
                    if (!(newGrab.chattersGlobal.isEqual(oldGrab.chattersGlobal))){
                        newGrab.previous = oldGrab;
                        oldGrab.previous = null;
                        updated.add(newGrab);
                    }
                    i.remove();
                    j.remove();
                    break;
                }
            }
        }

        updated.addAll(newGrabs);
        outRemainOlds.addAll(oldGrabs);

        return updated;
    }

    private float getAvgTimePerChannelSec(StatelessSession session, int processCirclesCount){
        var q = session.createNativeQuery("select AVG(q.time) as `time` from (select TIME_TO_SEC(TIMEDIFF(endTime, startTime))/totalChannels as `time` from `twitch-collector`.`circles` where `endTime` IS NOT NULL order by endTime desc limit :lim) as q");
        q.setParameter("lim", processCirclesCount);
        var res = (BigDecimal) q.getSingleResult();
        if (res == null){
            return 0;
        }
        return res.floatValue();
    }

    private HashMap<String, UserTypeEntity> getUsersTypes(StatelessSession session) {
        HashMap<String, UserTypeEntity> usersTypes = new HashMap<>();
        var query = session.createQuery("from UserTypeEntity", UserTypeEntity.class);
        var list = query.list();
        for (var i : list) {
            usersTypes.put(i.type, i);
        }
        return usersTypes;
    }

    private Pair<CircleEntity, CircleEntity> doCurrentCircle(StatelessSession session, int grabDuration) {
        var lastCircleQuery = session.createQuery("FROM CircleEntity order by number desc", CircleEntity.class);
        lastCircleQuery.setMaxResults(1);
        var lastCircle = lastCircleQuery.uniqueResult();

        CircleEntity currentCircle;

        if (lastCircle == null) {
            currentCircle = new CircleEntity();
            currentCircle.startTime = circleStartTime;
            currentCircle.totalChannels = 0;
            currentCircle.number = 1L;
        } else {
            currentCircle = new CircleEntity();
            currentCircle.startTime = circleStartTime;
            currentCircle.totalChannels = 0;
            currentCircle.number = lastCircle.number + 1;
        }

        currentCircle.grabDuration = grabDuration;

        session.beginTransaction();
        session.insert(currentCircle);
        session.getTransaction().commit();

        return new Pair<>(currentCircle, lastCircle);
    }

    private Pair<ChannelEntity, Boolean> doCurrentChannel(StatelessSession session, CircleEntity currentCircle, GrabChannelResult grabCh) {
        Pair<ChannelEntity, Boolean> pair = new Pair<>();
        var channelsQuery = session.createQuery("FROM ChannelEntity where name = :name and site = :site", ChannelEntity.class);
        channelsQuery.setMaxResults(1);
        channelsQuery.setParameter("name", grabCh.channelName);
        channelsQuery.setParameter("site", platformScope.get(grabCh.platform));
        var currentChannel = channelsQuery.uniqueResult();

        if (currentChannel == null) {
            currentChannel = new ChannelEntity();
            currentChannel.lastCheckedTime = grabCh.timestamp;
            currentChannel.name = grabCh.channelName;
            currentChannel.lastCircle = currentCircle;
            currentChannel.site = platformScope.get(grabCh.platform);
            pair.second = true;
        } else {
            currentChannel.lastCheckedTime = grabCh.timestamp;
            currentChannel.lastCircle = currentCircle;
            pair.second = false;
        }
        pair.first = currentChannel;

        return pair;
    }

    private ChannelCircleEntity doChannelCircle(CircleEntity currentCircle, ChannelEntity currentChannel, GrabChannelResult grabCh) {
        ChannelCircleEntity channelCircle = new ChannelCircleEntity();
        channelCircle.circle = currentCircle;
        channelCircle.channel = currentChannel;
        channelCircle.collectTime = grabCh.timestamp;
        channelCircle.chattersCount = grabCh.chattersGlobal.chatterCount;
        return channelCircle;
    }

    private ChannelCircleEntity getLastChannelCircle(StatelessSession session, CircleEntity lastCircle, ChannelEntity currentChannel) {
        var query = session.createQuery("from ChannelCircleEntity where circle = :circle and channel = :channel", ChannelCircleEntity.class);
        query.setParameter("circle", lastCircle);
        query.setParameter("channel", currentChannel);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    /**
     * Добавление пользователей
     * <p>
     * Возвращает:
     * Первое число - вставки
     * Второе число - апдейты
     */
    private Pair<Integer, Integer> doUsers(StatelessSession session, GrabChannelResult grabCh) {
        Pair<Integer, Integer> counts = new Pair<>(0, 0);
        ArrayList<String> allUsers = new ArrayList<>();
        for (var arr : grabCh.chattersGlobal.chatters.arrMap.values()){
            allUsers.addAll(arr);
        }

        session.beginTransaction();
        for (String user : allUsers) {
            /*UserEntity userEntity = new UserEntity();
            userEntity.name = user;
            userEntity.site = platformScope.get(grabCh.platform);
            userEntity.lastVisit = grabCh.timestamp;*/

            var query = session.createNativeQuery("select manageUsers(:name, :site, :lastVisit);");
            query.setParameter("name", user);
            query.setParameter("site", platformScope.get(grabCh.platform).id);
            query.setParameter("lastVisit", grabCh.timestamp);
            var res = (byte) query.getSingleResult();
            if (res == 1){
                counts.second++;
            }else if (res == 2){
                counts.first++;
            }

            /*try {
                session.insert(userEntity);
                counts.first++;
            } catch (Exception ignored) {
                counts.second++;
            }*/
        }
        session.getTransaction().commit();

        return counts;
    }

    /**
     * Связывание users и channels
     */
    private List<Pair<Long, Long>> doUsersChannels(StatelessSession session, ChannelEntity currentChannel, CircleEntity currentCircle, ChannelCircleEntity lastChannelCircle, HashMap<String, UserTypeEntity> userTypes, GrabChannelResult grabCh, float calculatedDuration) throws ExecutionException, InterruptedException {
        CircleEntity preCircle = null;

        if (currentCircle.number != 1) {
            var preCircleQuery = session.createQuery("from CircleEntity where number = :num", CircleEntity.class);
            preCircleQuery.setParameter("num", currentCircle.number - 1);
            var preCircles = preCircleQuery.list();

            if (!preCircles.isEmpty()) {
                preCircle = preCircles.get(0);
            }
        }

        final var site = platformScope.get(grabCh.platform);

        //Multithreading

        int usersInPart = Math.max(100, (int) Math.ceil(grabCh.chattersGlobal.chatterCount / (double) threadsToUse));
        List<CompletableFuture<Pair<Long, Long>>> futures = new ArrayList<>();
        final CircleEntity preC = preCircle;

        for (var key : UserType.values()){
            var arr = grabCh.chattersGlobal.chatters.arrMap.get(key);

            int partsCount = (int) Math.ceil(arr.size() / (double) usersInPart);
            List<Pair<Integer, Integer>> parts = new ArrayList<>();
            for (int i = 0; i < partsCount; i++) {
                Pair<Integer, Integer> pair = new Pair<>();
                pair.first = i * usersInPart;
                if (i == partsCount - 1) {
                    pair.second = arr.size();
                } else {
                    pair.second = (i + 1) * usersInPart;
                }
                parts.add(pair);
            }

            for (final var part : parts) {
                final String[] collection = arr.stream().skip(part.first).limit(part.second - part.first).toArray(String[]::new);
                CompletableFuture<Pair<Long, Long>> f = CompletableFuture.supplyAsync(() -> {
                    Pair<Long, Long> pair = new Pair<>(0L, 0L);
                    try(StatelessSession s = getSession()) {
                        updateViewers(pair, s, currentChannel, currentCircle, preC, lastChannelCircle, collection, userTypes.get(key.dbName), grabCh, site, calculatedDuration);
                    }
                    return pair;
                }, pool).exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
                futures.add(f);
            }
        }

        var future = FutureUtils.allOf(futures);
        var result = future.get();
        //result.add(new Pair<>(toUpdate, toInsert));

        return result;
    }

    private void updateViewers(Pair<Long, Long> stats, StatelessSession session, ChannelEntity channel, CircleEntity currentCircle, CircleEntity preCircle, ChannelCircleEntity lastChannelCircle, String[] names, UserTypeEntity type, GrabChannelResult grab, SiteEntity site, float calculatedDuration) {
        HashMap<Pair<Long, Long>, ChannelCircleEntity> channelsCirclesHash = new HashMap<>();

        session.beginTransaction();
        for (String name : names) {
            /*
            //boolean b_userChannel = false;
            //boolean b_lastChannelCircle = false;
            //boolean b_fitsToUpdate = false;
            if (preCircle != null) {
                var query = session.createNativeQuery("select * from (select * from `twitch-collector`.users_channels where user_id = (select id from `twitch-collector`.users where name = :name and site_id = :site_id) and channel_id = :channel_id and type_id = :userType) as q join `twitch-collector`.circles on (q.lastCircle_id = circles.id) where endTime is not null order by endTime desc", UserChannelEntity.class);
                query.setParameter("name", name);
                query.setParameter("channel_id", channel.id);
                query.setParameter("userType", type.id);
                query.setParameter("site_id", site.id);
                query.setFirstResult(0);
                query.setMaxResults(1);
                UserChannelEntity userChannel = null;
                try {
                    userChannel = query.uniqueResult();
                }catch (Throwable t){
                    writeLog(LogStatus.Error, String.format("userChannel uniqueResult error: %s %d %d | %s", name, channel.id, type.id, DataUtil.getStackTrace(t)));
                    t.printStackTrace();
                    continue;
                }
                if (userChannel != null) {
                    //b_userChannel = true;

                    // Fits by time
                    boolean fitsByPrevious = false;
                    if (grab.previous != null) {
                        var set = grab.previous.chattersGlobal.chatters.getSetByUserType(type.type);
                        if (set != null){
                            fitsByPrevious = set.contains(name);
                        }
                    }
                    ChannelCircleEntity lastUserChannelCircle = null;
                    if (!fitsByPrevious) {
                        var key = new Pair<>(userChannel.lastCircle.id, channel.id);
                        if (channelsCirclesHash.containsKey(key)) {
                            lastUserChannelCircle = channelsCirclesHash.get(key);
                        } else {
                            var q = session.createNativeQuery("select * from `twitch-collector`.channels_circles where circle_id = :circle and channel_id = :channel", ChannelCircleEntity.class);
                            q.setParameter("circle", userChannel.lastCircle.id);
                            q.setParameter("channel", channel.id);
                            lastUserChannelCircle = q.uniqueResult();
                            if (lastUserChannelCircle != null)
                                channelsCirclesHash.put(key, lastUserChannelCircle);
                        }
                    }
                    if (fitsByPrevious || lastUserChannelCircle != null) {
                        //b_lastChannelCircle = true;

                        if (fitsByPrevious){
                            //writeLog(LogStatus.Warning, "fitsByPrevious = true; " + name);
                        }

                        boolean fitsToUpdate = fitsByPrevious || circleStartTime.minusSeconds((long) Math.max(viewerLeaveSec, calculatedDuration)).compareTo(lastUserChannelCircle.collectTime) < 0;
                        if (fitsToUpdate) {
                            //b_fitsToUpdate = true;
                            //Add to update
                            userChannel.lastCircle = currentCircle;
                            toUpdate.add(userChannel);
                            continue;
                        }
                    }
                }
            }

            var query = session.createNativeQuery("select * from `twitch-collector`.users where name = :name and site_id = :site_id", UserEntity.class);
            query.setParameter("name", name);
            query.setParameter("site_id", site.id);
            query.setFirstResult(0);
            query.setMaxResults(1);
            var user = query.uniqueResult();

            if (user == null)
                return;

            //Add to insert
            var userChannel = new UserChannelEntity();
            userChannel.user = user;
            userChannel.channel = channel;
            userChannel.firstCircle = currentCircle;
            userChannel.lastCircle = currentCircle;
            userChannel.type = type;
            toInsert.add(userChannel);

            //writeLog(LogStatus.Warning, String.format("test - %b %b %b", b_userChannel, b_lastChannelCircle, b_fitsToUpdate));
            */

            boolean fitsByPrevious = false;
            if (grab.previous != null) {
                var set = grab.previous.chattersGlobal.chatters.getSetByUserType(type.type);
                if (set != null){
                    fitsByPrevious = set.contains(name);
                }
            }

            var query = session.createNativeQuery("select manageUsersChannels(:ch_id, :cur_cir_id, :pre_cir_id, :name, :type, :site, :fits_by_prev, :datetime);");
            query.setParameter("ch_id", channel.id);
            query.setParameter("cur_cir_id", currentCircle.id);
            query.setParameter("pre_cir_id", preCircle == null ? 0 : preCircle.id);
            query.setParameter("name", name);
            query.setParameter("type", type.id);
            query.setParameter("site", site.id);
            query.setParameter("fits_by_prev", fitsByPrevious);
            query.setParameter("datetime", grab.timestamp);
            var res = (byte) query.getSingleResult();
            if (res == 1){
                stats.second++;
            }else if (res == 2){
                stats.first++;
            }
        }
        session.getTransaction().commit();
    }
}
