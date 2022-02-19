package service;

import database.entities.*;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import util.FutureUtils;
import util.Pair;
import util.TimeUtil;
import util.TwitchGrabber;
import util.grabber.GrabChannelError;
import util.grabber.GrabChannelResult;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CircleService extends AbstractService{

    public CircleService() {
        super("CircleService");
    }

    private ZonedDateTime circleStartTime;

    @Override
    protected void work() {
        StatelessSession session = database.DatabaseUtil.getStateLessSession();
        try {
            while (true) {
                sleep(1000);
                TwitchGrabber grabber = new TwitchGrabber(0);

                var query = session.createQuery("FROM ChannelToCheckEntity ORDER BY priority asc", ChannelToCheckEntity.class);
                var channels = query.list();

                if (channels.isEmpty())
                    continue;

                for (var channel : channels){
                    grabber.getChannelsToGrab().add(channel.name);
                }
                circleStartTime = TimeUtil.getZonedNow();
                grabber.startGrabAsyncHttp();
                var grabbedChannels = grabber.getResults();

                int errorsCount = 0;
                for (var grabCh : grabbedChannels){
                    if (grabCh.error != GrabChannelError.None || grabCh.chattersGlobal == null)
                        errorsCount++;
                }
                if (errorsCount == grabbedChannels.size()) {
                    continue;
                }

                var currentCircle = doCurrentCircle(session);


                for (var grabCh : grabbedChannels){

                    var currentChannel = doCurrentChannel(session, currentCircle, grabCh);

                    doUsers(session, grabCh);

                    doUsersChannels(session, currentChannel, currentCircle, grabCh);

                    session.beginTransaction();
                    currentCircle.totalChannels++;
                    session.update(currentCircle);
                    session.getTransaction().commit();
                }

                session.beginTransaction();
                currentCircle.endTime = TimeUtil.getZonedNow();
                session.update(currentCircle);
                session.getTransaction().commit();

                System.out.println("Done");
                sleep(100000);
            }
        }catch (Exception e){
            session.close();
            e.printStackTrace();
        }
    }

    public CircleEntity doCurrentCircle(StatelessSession session){
        var lastCircleQuery = session.createQuery("FROM CircleEntity order by number desc", CircleEntity.class);
        lastCircleQuery.setMaxResults(1);
        var lastCircle = lastCircleQuery.uniqueResult();

        CircleEntity currentCircle;

        if (lastCircle == null){
            currentCircle = new CircleEntity();
            currentCircle.startTime = circleStartTime;
            currentCircle.totalChannels = 0;
            currentCircle.number = 1L;

            session.beginTransaction();
            session.insert(currentCircle);
            session.getTransaction().commit();
        }else{
            currentCircle = new CircleEntity();
            currentCircle.startTime = circleStartTime;
            currentCircle.totalChannels = 0;
            currentCircle.number = lastCircle.number + 1;

            session.beginTransaction();
            session.insert(currentCircle);
            session.getTransaction().commit();
        }

        return currentCircle;
    }

    public ChannelEntity doCurrentChannel(StatelessSession session, CircleEntity currentCircle, GrabChannelResult grabCh){
        var channelsQuery = session.createQuery("FROM ChannelEntity where name = :name", ChannelEntity.class);
        channelsQuery.setMaxResults(1);
        channelsQuery.setParameter("name", grabCh.channelName);
        var currentChannel = channelsQuery.uniqueResult();

        if (currentChannel == null){
            currentChannel = new ChannelEntity();
            currentChannel.lastCheckedTime = grabCh.timestamp;
            currentChannel.name = grabCh.channelName;
            currentChannel.lastCircle = currentCircle;

            session.beginTransaction();
            session.insert(currentChannel);
            session.getTransaction().commit();
        }else{
            currentChannel.lastCheckedTime = grabCh.timestamp;
            currentChannel.lastCircle = currentCircle;

            session.beginTransaction();
            session.update(currentChannel);
            session.getTransaction().commit();
        }

        return currentChannel;
    }

    /**
     * Добавление пользователей
     * @param session
     * @param grabCh
     */
    public void doUsers(StatelessSession session, GrabChannelResult grabCh){
        ArrayList<String> allUsers = new ArrayList<>();
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.viewers));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.moderators));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.admins));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.broadcaster));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.staff));
        allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.vips));

        session.beginTransaction();
        for (String user : allUsers){
            UserEntity userEntity = new UserEntity();
            userEntity.name = user;
            try {
                session.insert(userEntity);
            }catch (Exception e){}
        }
        session.getTransaction().commit();
    }

    /**
     * Связывание users и channels
     * @param currentCircle
     * @param session
     */
    private void doUsersChannels(StatelessSession session,ChannelEntity currentChannel, CircleEntity currentCircle, GrabChannelResult grabCh) throws ExecutionException, InterruptedException {
        CircleEntity preCircle = null;

        if (currentCircle.number != 1){
            session.beginTransaction();
            var preCircleQuery = session.createQuery("from CircleEntity where number = :num", CircleEntity.class);
            preCircleQuery.setParameter("num", currentCircle.number-1);
            var preCircles = preCircleQuery.list();
            session.getTransaction().commit();

            if (!preCircles.isEmpty()){
                preCircle = preCircles.get(0);
            }
        }

        //Multithreading
        var startTime = System.currentTimeMillis();
        List<UserChannelEntity> toUpdate = new ArrayList<>();
        List<UserChannelEntity> toInsert = new ArrayList<>();
        updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.moderators, "moderator");
        updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.admins, "admin");
        updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.broadcaster, "broadcaster");
        updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.staff, "staff");
        updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.vips, "vip");

        final CircleEntity preC = preCircle;
        int partsCount = 16;
        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Pair<Integer,Integer>> parts = new ArrayList<>();
        int lenPerPart = grabCh.chattersGlobal.chatters.viewers.length / partsCount;
        for (int i = 0; i < partsCount; i++){
            Pair<Integer,Integer> pair = new Pair<>();
            pair.first = i * lenPerPart;
            if (i == partsCount - 1){
                pair.second = grabCh.chattersGlobal.chatters.viewers.length;
            }else{
                pair.second = (i+1) * lenPerPart;
            }
            parts.add(pair);
        }

        List<CompletableFuture<Pair<List<UserChannelEntity>,List<UserChannelEntity>>>> futures = new ArrayList<>();
        for (final var part : parts) {
            final String[] collection = Arrays.stream(grabCh.chattersGlobal.chatters.viewers).skip(part.first).limit(part.second-part.first).toArray(String[]::new);
            CompletableFuture<Pair<List<UserChannelEntity>,List<UserChannelEntity>>> f = CompletableFuture.supplyAsync(() -> {
                Pair<List<UserChannelEntity>,List<UserChannelEntity>> pair = new Pair<>();
                pair.first = new ArrayList<>();
                pair.second = new ArrayList<>();
                StatelessSession s = database.DatabaseUtil.getStateLessSession();
                updateViewers(grabCh, pair.first, pair.second, s, currentChannel, currentCircle, preC, collection, "viewer");
                s.close();
                return pair;
            }, pool).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
            futures.add(f);
        }

        var future = FutureUtils.allOf(futures);

        var results = future.get();

        System.out.println(System.currentTimeMillis()-startTime + " ms - Update viewers (prepare)");
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
        System.out.println(System.currentTimeMillis()-startTime + " ms - Update viewers (insert and update)");
    }

    private void updateViewers(GrabChannelResult grabCh, List<UserChannelEntity> toUpdate, List<UserChannelEntity> toInsert, StatelessSession session, ChannelEntity channel, CircleEntity currentCircle, CircleEntity preCircle, String[] names, String type){
        for (String name : names){
            if (preCircle != null) {
                var query = session.createNativeQuery("select * from `twitch-collector`.users_channels where user_id = (select user_id from `twitch-collector`.users where name = :name) and lastCircle_id = :preCircle_id and channel_id = :channel_id and type = :userType", UserChannelEntity.class);
                //var query = session.createQuery("from UserChannelEntity where user.name = :name and channel.id = :channel_id and type = :userType and lastCircle.id = :preCircle_id", UserChannelEntity.class);
                query.setParameter("name", name);
                query.setParameter("channel_id", channel.id);
                query.setParameter("userType", type);
                query.setParameter("preCircle_id", preCircle.id);
                query.setFirstResult(0);
                query.setMaxResults(1);
                var userChannel = query.uniqueResult();
                if (userChannel != null) {
                    boolean fitsByTime = circleStartTime.minusSeconds(10 * 60).compareTo(userChannel.lastOnlineTime) < 0;
                    if (fitsByTime) {
                        //Add to update
                        userChannel.lastCircle = currentCircle;
                        userChannel.lastOnlineTime = grabCh.timestamp;
                        toUpdate.add(userChannel);
                        continue;
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
            userChannel.firstOnlineTime = grabCh.timestamp;
            userChannel.lastOnlineTime = userChannel.firstOnlineTime;
            toInsert.add(userChannel);
        }
    }
}
