package service;

import database.entities.*;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import util.TimeUtil;
import util.TwitchGrabber;
import util.grabber.GrabChannelError;
import util.grabber.GrabChannelResult;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

                Transaction tx = session.beginTransaction();
                var query = session.createQuery("FROM ChannelToCheckEntity ORDER BY priority asc", ChannelToCheckEntity.class);
                var channels = query.list();
                tx.commit();

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
                if (errorsCount == grabbedChannels.size())
                    continue;


                tx = session.beginTransaction();
                var lastCircleQuery = session.createQuery("FROM CircleEntity order by number desc", CircleEntity.class);
                lastCircleQuery.setMaxResults(1);
                var lastCircleList = lastCircleQuery.list();
                tx.commit();

                CircleEntity currentCircle;

                if (lastCircleList.isEmpty()){
                    currentCircle = new CircleEntity();
                    currentCircle.startTime = circleStartTime;
                    currentCircle.totalChannels = 0;
                    currentCircle.number = 1L;
                    tx = session.beginTransaction();
                    session.insert(currentCircle);
                    tx.commit();
                }else{
                    CircleEntity lastCircle = lastCircleList.get(0);
                    currentCircle = new CircleEntity();
                    currentCircle.startTime = circleStartTime;
                    currentCircle.totalChannels = 0;
                    currentCircle.number = lastCircle.number + 1;
                    tx = session.beginTransaction();
                    session.insert(currentCircle);
                    tx.commit();
                }

                for (var grabCh : grabbedChannels){
                    tx = session.beginTransaction();
                    var channelsQuery = session.createQuery("FROM ChannelEntity where name = :name", ChannelEntity.class);
                    channelsQuery.setMaxResults(1);
                    channelsQuery.setParameter("name", grabCh.channelName);
                    var channelsList = channelsQuery.list();
                    tx.commit();

                    ChannelEntity currentChannel;

                    if (channelsList.isEmpty()){
                        currentChannel = new ChannelEntity();
                        currentChannel.lastCheckedTime = grabCh.timestamp;
                        currentChannel.name = grabCh.channelName;
                        currentChannel.lastCircle = currentCircle;
                        tx = session.beginTransaction();
                        session.insert(currentChannel);
                        tx.commit();
                    }else{
                        currentChannel = channelsList.get(0);
                        currentChannel.lastCheckedTime = grabCh.timestamp;
                        currentChannel.lastCircle = currentCircle;
                        tx = session.beginTransaction();
                        session.update(currentChannel);
                        tx.commit();
                    }

                    //Добавление пользователей

                    ArrayList<String> allUsers = new ArrayList<>();
                    allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.viewers));
                    allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.moderators));
                    allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.admins));
                    allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.broadcaster));
                    allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.staff));
                    allUsers.addAll(Arrays.asList(grabCh.chattersGlobal.chatters.vips));

                    tx = session.beginTransaction();
                    for (String user : allUsers){
                        UserEntity userEntity = new UserEntity();
                        userEntity.name = user;
                        try {
                            session.insert(userEntity);
                        }catch (Exception e){}
                    }
                    tx.commit();

                    //Связывание users и channels

                    CircleEntity preCircle = null;

                    if (currentCircle.number != 1){
                        tx = session.beginTransaction();
                        var preCircleQuery = session.createQuery("from CircleEntity where number = :num", CircleEntity.class);
                        preCircleQuery.setParameter("num", currentCircle.number-1);
                        var preCircles = preCircleQuery.list();
                        tx.commit();

                        if (!preCircles.isEmpty()){
                            preCircle = preCircles.get(0);
                        }
                    }



                    //int userCount = grabCh.chattersGlobal.calcUsersCount();

                    List<UserChannelEntity> toUpdate = new ArrayList<>();
                    List<UserChannelEntity> toInsert = new ArrayList<>();

                    updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.viewers, "viewer");
                    updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.moderators, "moderator");
                    updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.admins, "admin");
                    updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.broadcaster, "broadcaster");
                    updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.staff, "staff");
                    updateViewers(grabCh, toUpdate, toInsert, session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.vips, "vip");

                    tx = session.beginTransaction();
                    for (var user : toUpdate){
                        session.update(user);
                    }
                    for (var user : toInsert){
                        session.insert(user);
                    }
                    tx.commit();



                    tx = session.beginTransaction();
                    //currentChannel.lastCheckedTime = TimeUtil.getZonedNow();
                    //session.update(currentChannel);

                    currentCircle.totalChannels++;
                    session.update(currentCircle);
                    tx.commit();
                }

                tx = session.beginTransaction();
                currentCircle.endTime = TimeUtil.getZonedNow();
                session.update(currentCircle);
                tx.commit();

                System.out.println("Done");
                sleep(100000);
            }
        }catch (Exception e){
            session.close();
            e.printStackTrace();
        }
    }

    private void updateViewers(GrabChannelResult grabCh, List<UserChannelEntity> toUpdate, List<UserChannelEntity> toInsert, StatelessSession session, ChannelEntity channel, CircleEntity currentCircle, CircleEntity preCircle, String[] names, String type){
        for (String name : names){
            //System.out.println(name);
            if (preCircle != null) {
                var query = session.createQuery("from UserChannelEntity where user.name = :name and channel.id = :channel_id and type = :userType and lastCircle.id = :preCircle_id", UserChannelEntity.class);
                query.setParameter("name", name);
                query.setParameter("channel_id", channel.id);
                query.setParameter("userType", type);
                query.setParameter("preCircle_id", preCircle.id);
                var usersChannels = query.list();
                if (!usersChannels.isEmpty()) {
                    var userChannel = usersChannels.get(0);
                    boolean fitsByTime = circleStartTime.minusSeconds(10 * 60).compareTo(userChannel.lastOnlineTime) < 0;
                    if (fitsByTime) {
                        //Update
                        userChannel.lastCircle = currentCircle;
                        userChannel.lastOnlineTime = grabCh.timestamp;
                        //tx = session.beginTransaction();
                        //session.update(userChannel);
                        //tx.commit();
                        toUpdate.add(userChannel);
                        continue;
                    }
                }
            }

            var query = session.createQuery("from UserEntity where name = :name", UserEntity.class);
            query.setParameter("name", name);
            var users = query.list();

            if (users.isEmpty())
                return;

            //Insert
            var userChannel = new UserChannelEntity();
            userChannel.user = users.get(0);
            userChannel.channel = channel;
            userChannel.firstCircle = currentCircle;
            userChannel.lastCircle = currentCircle;
            userChannel.type = type;
            userChannel.firstOnlineTime = grabCh.timestamp;
            userChannel.lastOnlineTime = userChannel.firstOnlineTime;
            //tx = session.beginTransaction();
            //session.insert(userChannel);
            //tx.commit();
            toInsert.add(userChannel);
        }
    }
}
