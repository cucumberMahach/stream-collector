package service;

import database.entities.*;
import logging.LogStatus;
import org.hibernate.StatelessSession;
import util.*;
import util.grabber.GrabChannelResult;
import util.grabber.TwitchGrabber;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;

public class CircleService extends AbstractService {

    public CircleService() {
        super("circle");
    }

    private ZonedDateTime circleStartTime;
    public int threadsToUse = Runtime.getRuntime().availableProcessors();
    public int toNextCircleMs = 60000;

    @Override
    protected void work() {
        StatelessSession session = database.DatabaseUtil.getStateLessSession();
        try {
            while (true) {
                TwitchGrabber grabber = new TwitchGrabber(0);

                var query = session.createQuery("FROM ChannelToCheckEntity ORDER BY priority asc", ChannelToCheckEntity.class);
                var channels = query.list();

                if (channels.isEmpty()) {
                    writeLog(LogStatus.Warning, "В БД нет каналов для сбора информации");
                    sleep(10000);
                    continue;
                }

                for (var channel : channels) {
                    grabber.getChannelsToGrab().add(channel.name);
                }
                circleStartTime = TimeUtil.getZonedNow();
                grabber.startGrabAsyncHttp();
                var grabbedChannels = grabber.getResults();

                int errorsCount = 0;
                for (var grabCh : grabbedChannels) {
                    if (grabCh.isError()) {
                        errorsCount++;
                        writeLog(LogStatus.Error, "Ошибка при сборе информации о канале " + grabCh.channelName + ": " + grabCh.getError());
                    }
                }
                if (errorsCount == grabbedChannels.size()) {
                    writeLog(LogStatus.Error, "При сборе информации во всех каналах возникли ошибки");
                    sleep(10000);
                    continue;
                }

                var maxTime = getMaxTimePerChannel(session, 10); //TODO

                var userTypes = getUsersTypes(session);

                Pair<CircleEntity, CircleEntity> currentAndLastCircles = doCurrentCircle(session);
                var currentCircle = currentAndLastCircles.first;
                var lastCircle = currentAndLastCircles.second;

                for (var grabCh : grabbedChannels) {
                    if (grabCh.isError())
                        continue;

                    writeLog(LogStatus.None, "Запись канала " + grabCh.channelName);

                    var currentChannelPair = doCurrentChannel(session, currentCircle, grabCh);
                    var currentChannel = currentChannelPair.first;

                    ChannelCircleEntity lastChannelCircle = null;
                    if (lastCircle != null && !currentChannelPair.second) {
                        lastChannelCircle = getLastChannelCircle(session, lastCircle, currentChannel);
                    }

                    var channelCircle = doChannelCircle(currentCircle, currentChannel, grabCh);

                    var startTime = System.currentTimeMillis();
                    var usersStatistic = doUsers(session, grabCh);

                    writeLog(LogStatus.Success, String.format("Пользователи записаны (inserts - %d, ignored - %d, all - %d): %d мс", usersStatistic.first, usersStatistic.second, usersStatistic.first + usersStatistic.second, System.currentTimeMillis() - startTime));


                    startTime = System.currentTimeMillis();
                    var usersChannels = doUsersChannels(session, currentChannel, currentCircle, lastChannelCircle, userTypes, grabCh);
                    writeLog(LogStatus.Success, String.format("Подготовка зрителей к записи: %d мс", System.currentTimeMillis() - startTime));

                    //================================FINAL TRANSACTION================================
                    session.beginTransaction();

                    //Channel
                    if (currentChannelPair.second) {
                        session.insert(currentChannel);
                    } else {
                        session.update(currentChannel);
                    }

                    //Channel Circle
                    session.insert(channelCircle);

                    //Users Channels
                    startTime = System.currentTimeMillis();
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
                    }
                    writeLog(LogStatus.Success, String.format("Запись зрителей (inserts - %d, updates - %d, all - %d): %d мс", usersInserts, usersUpdate, usersInserts + usersUpdate, System.currentTimeMillis() - startTime));

                    //Current Circle update
                    currentCircle.totalChannels++;
                    session.update(currentCircle);
                    session.getTransaction().commit();
                    //=================================================================================
                }

                session.beginTransaction();
                currentCircle.endTime = TimeUtil.getZonedNow();
                session.update(currentCircle);
                session.getTransaction().commit();

                writeLog(LogStatus.Success, String.format("Обработано каналов: %d (%s)", currentCircle.totalChannels, TimeUtil.formatDuration(Duration.between(currentCircle.startTime, currentCircle.endTime))));
                sleep(toNextCircleMs);
            }
        } catch (Exception e) {
            session.close();
            if (!(e instanceof InterruptedException))
                writeLog(LogStatus.Error, "Исключение в сервисе: " + e.getMessage() + " " + DataUtil.getStackTrace(e));
        }
    }

    /**
     * @return Время в миллисекундах
     */
    private int getMaxTimePerChannel(StatelessSession session, int processCirclesCount){
        var q = session.createNativeQuery("select MAX(q.time) as `time` from (select TIME_TO_SEC(TIMEDIFF(endTime, startTime))/totalChannels as `time` from `twitch-collector`.`circles` where `endTime` IS NOT NULL order by endTime desc limit :lim) as q");
        q.setParameter("lim", processCirclesCount);
        var res = (BigDecimal) q.getSingleResult();
        return (int) (res.floatValue() * 1000);
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

    private Pair<CircleEntity, CircleEntity> doCurrentCircle(StatelessSession session) {
        var lastCircleQuery = session.createQuery("FROM CircleEntity order by number desc", CircleEntity.class);
        lastCircleQuery.setMaxResults(1);
        var lastCircle = lastCircleQuery.uniqueResult();

        CircleEntity currentCircle;

        if (lastCircle == null) {
            currentCircle = new CircleEntity();
            currentCircle.startTime = circleStartTime;
            currentCircle.totalChannels = 0;
            currentCircle.number = 1L;

            session.beginTransaction();
            session.insert(currentCircle);
            session.getTransaction().commit();
        } else {
            currentCircle = new CircleEntity();
            currentCircle.startTime = circleStartTime;
            currentCircle.totalChannels = 0;
            currentCircle.number = lastCircle.number + 1;

            session.beginTransaction();
            session.insert(currentCircle);
            session.getTransaction().commit();
        }

        return new Pair<>(currentCircle, lastCircle);
    }

    private Pair<ChannelEntity, Boolean> doCurrentChannel(StatelessSession session, CircleEntity currentCircle, GrabChannelResult grabCh) {
        Pair<ChannelEntity, Boolean> pair = new Pair<>();
        var channelsQuery = session.createQuery("FROM ChannelEntity where name = :name", ChannelEntity.class);
        channelsQuery.setMaxResults(1);
        channelsQuery.setParameter("name", grabCh.channelName);
        var currentChannel = channelsQuery.uniqueResult();

        if (currentChannel == null) {
            currentChannel = new ChannelEntity();
            currentChannel.lastCheckedTime = grabCh.timestamp;
            currentChannel.name = grabCh.channelName;
            currentChannel.lastCircle = currentCircle;
            pair.second = true;

            /*session.beginTransaction();
            session.insert(currentChannel);
            session.getTransaction().commit();*/
        } else {
            currentChannel.lastCheckedTime = grabCh.timestamp;
            currentChannel.lastCircle = currentCircle;
            pair.second = false;

            /*session.beginTransaction();
            session.update(currentChannel);
            session.getTransaction().commit();*/
        }
        pair.first = currentChannel;

        return pair;
    }

    private ChannelCircleEntity doChannelCircle(CircleEntity currentCircle, ChannelEntity currentChannel, GrabChannelResult grabCh) {
        ChannelCircleEntity channelCircle = new ChannelCircleEntity();
        channelCircle.circle = currentCircle;
        channelCircle.channel = currentChannel;
        channelCircle.collectTime = grabCh.timestamp;
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
     * Второе число - проигнорировано
     */
    private Pair<Integer, Integer> doUsers(StatelessSession session, GrabChannelResult grabCh) {
        Pair<Integer, Integer> counts = new Pair<>(0, 0);
        ArrayList<String> allUsers = new ArrayList<>();
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.viewers));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.moderators));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.admins));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.broadcaster));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.staff));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.vips));

        session.beginTransaction();
        for (String user : allUsers) {
            UserEntity userEntity = new UserEntity();
            userEntity.name = user;
            try {
                session.insert(userEntity);
                counts.first++;
            } catch (Exception ignored) {
                counts.second++;
            }
        }
        session.getTransaction().commit();

        return counts;
    }

    /**
     * Связывание users и channels
     */
    private List<Pair<List<UserChannelEntity>, List<UserChannelEntity>>> doUsersChannels(StatelessSession session, ChannelEntity currentChannel, CircleEntity currentCircle, ChannelCircleEntity lastChannelCircle, HashMap<String, UserTypeEntity> userTypes, GrabChannelResult grabCh) throws ExecutionException, InterruptedException {
        CircleEntity preCircle = null;

        if (currentCircle.number != 1) {
            session.beginTransaction();
            var preCircleQuery = session.createQuery("from CircleEntity where number = :num", CircleEntity.class);
            preCircleQuery.setParameter("num", currentCircle.number - 1);
            var preCircles = preCircleQuery.list();
            session.getTransaction().commit();

            if (!preCircles.isEmpty()) {
                preCircle = preCircles.get(0);
            }
        }

        //Multithreading
        List<UserChannelEntity> toUpdate = new ArrayList<>();
        List<UserChannelEntity> toInsert = new ArrayList<>();
        updateViewers(toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, lastChannelCircle, grabCh.chattersGlobal.chatters.moderators, userTypes.get("moderator"));
        updateViewers(toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, lastChannelCircle, grabCh.chattersGlobal.chatters.admins, userTypes.get("admin"));
        updateViewers(toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, lastChannelCircle, grabCh.chattersGlobal.chatters.broadcaster, userTypes.get("broadcaster"));
        updateViewers(toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, lastChannelCircle, grabCh.chattersGlobal.chatters.staff, userTypes.get("staff"));
        updateViewers(toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, lastChannelCircle, grabCh.chattersGlobal.chatters.vips, userTypes.get("vip"));

        final CircleEntity preC = preCircle;
        int partsCount = threadsToUse;
        ExecutorService pool = Executors.newFixedThreadPool(partsCount);
        List<Pair<Integer, Integer>> parts = new ArrayList<>();
        int lenPerPart = grabCh.chattersGlobal.chatters.viewers.length / partsCount;
        for (int i = 0; i < partsCount; i++) {
            Pair<Integer, Integer> pair = new Pair<>();
            pair.first = i * lenPerPart;
            if (i == partsCount - 1) {
                pair.second = grabCh.chattersGlobal.chatters.viewers.length;
            } else {
                pair.second = (i + 1) * lenPerPart;
            }
            parts.add(pair);
        }

        List<CompletableFuture<Pair<List<UserChannelEntity>, List<UserChannelEntity>>>> futures = new ArrayList<>();
        for (final var part : parts) {
            final String[] collection = Arrays.stream(grabCh.chattersGlobal.chatters.viewers).skip(part.first).limit(part.second - part.first).toArray(String[]::new);
            CompletableFuture<Pair<List<UserChannelEntity>, List<UserChannelEntity>>> f = CompletableFuture.supplyAsync(() -> {
                Pair<List<UserChannelEntity>, List<UserChannelEntity>> pair = new Pair<>();
                pair.first = new ArrayList<>();
                pair.second = new ArrayList<>();
                StatelessSession s = database.DatabaseUtil.getStateLessSession();
                updateViewers(pair.first, pair.second, s, currentChannel, currentCircle, preC, lastChannelCircle, collection, userTypes.get("viewer"));
                s.close();
                return pair;
            }, pool).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
            futures.add(f);
        }

        var future = FutureUtils.allOf(futures);
        var result = future.get();
        result.add(new Pair<>(toUpdate, toInsert));

        return result;

        /*System.out.println(System.currentTimeMillis()-startTime + " ms - Update viewers (prepare)");
        startTime = System.currentTimeMillis();

        session.beginTransaction();
        for (var r : results){
            if (r != null){
                for (var user : r.first){
                    session.update(user);
                }
                for (var user : r.second){
                    session.insert(user);
                }
            }
        }
        session.getTransaction().commit();
        System.out.println(System.currentTimeMillis()-startTime + " ms - Update viewers (insert and update)");*/
    }

    private void updateViewers(List<UserChannelEntity> toUpdate, List<UserChannelEntity> toInsert, StatelessSession session, ChannelEntity channel, CircleEntity currentCircle, CircleEntity preCircle, ChannelCircleEntity lastChannelCircle, String[] names, UserTypeEntity type) {
        HashMap<Pair<Long, Long>, ChannelCircleEntity> channelsCirclesHash = new HashMap<>();
        for (String name : names) {
            if (preCircle != null) {
                var query = session.createNativeQuery("select * from `twitch-collector`.users_channels where user_id = (select id from `twitch-collector`.users where name = :name) and lastCircle_id = :preCircle_id and channel_id = :channel_id and type_id = :userType", UserChannelEntity.class);
                //var query = session.createQuery("from UserChannelEntity where user.name = :name and channel.id = :channel_id and type = :userType and lastCircle.id = :preCircle_id", UserChannelEntity.class);
                query.setParameter("name", name);
                query.setParameter("channel_id", channel.id);
                query.setParameter("userType", type.id);
                query.setParameter("preCircle_id", preCircle.id);
                query.setFirstResult(0);
                query.setMaxResults(1);
                var userChannel = query.uniqueResult();
                if (userChannel != null) {
                    //Fits by time
                    var key = new Pair<>(userChannel.lastCircle.id, channel.id);
                    ChannelCircleEntity lastUserChannelCircle;
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
                    if (lastUserChannelCircle != null) {
                        boolean fitsByTime = circleStartTime.minusSeconds(10 * 60).compareTo(lastUserChannelCircle.collectTime) < 0;
                        if (fitsByTime) {
                            //Add to update
                            userChannel.lastCircle = currentCircle;
                            toUpdate.add(userChannel);
                            continue;
                        }
                    }
                }
            }

            var query = session.createNativeQuery("select * from `twitch-collector`.users where name = :name", UserEntity.class);
            //var query = session.createQuery("from UserEntity where name = :name", UserEntity.class);
            query.setParameter("name", name);
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
        }
    }
}
