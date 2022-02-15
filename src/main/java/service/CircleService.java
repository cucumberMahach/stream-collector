package service;

import database.entities.*;
import logging.LogStatus;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.type.LongType;
import util.TwitchGrabber;
import util.grabber.GrabChannelError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CircleService extends AbstractService{

    public CircleService() {
        super("CircleService");
    }

    @Override
    protected void work() {
        StatelessSession session = database.DatabaseUtil.getStateLessSession();
        try {
            while (true) {
                sleep(1000);
                TwitchGrabber grabber = new TwitchGrabber();

                Transaction tx = session.beginTransaction();
                var query = session.createQuery("FROM ChannelToCheckEntity ORDER BY priority asc", ChannelToCheckEntity.class);
                var channels = query.list();
                tx.commit();

                if (channels.isEmpty())
                    continue;

                for (var channel : channels){
                    grabber.getChannelsToGrab().add(channel.name);
                }
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
                    currentCircle.startTime = new Date();
                    currentCircle.totalChannels = 0;
                    currentCircle.number = 1L;
                    tx = session.beginTransaction();
                    session.insert(currentCircle);
                    tx.commit();
                }else{
                    CircleEntity lastCircle = lastCircleList.get(0);
                    currentCircle = new CircleEntity();
                    currentCircle.startTime = new Date();
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
                        currentChannel.lastCheckedTime = new Date();
                        currentChannel.name = grabCh.channelName;
                        currentChannel.lastCircle = currentCircle;
                        tx = session.beginTransaction();
                        session.insert(currentChannel);
                        tx.commit();
                    }else{
                        currentChannel = channelsList.get(0);
                        currentChannel.lastCheckedTime = new Date();
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

                    updateViewers(session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.viewers, "viewer");
                    updateViewers(session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.moderators, "moderator");
                    updateViewers(session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.admins, "admin");
                    updateViewers(session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.broadcaster, "broadcaster");
                    updateViewers(session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.staff, "staff");
                    updateViewers(session, currentChannel, currentCircle, preCircle, grabCh.chattersGlobal.chatters.vips, "vip");


                    tx = session.beginTransaction();
                    currentChannel.lastCheckedTime = new Date();
                    session.update(currentChannel);

                    currentCircle.totalChannels++;
                    session.update(currentCircle);
                    tx.commit();
                }

                tx = session.beginTransaction();
                currentCircle.endTime = new Date();
                session.update(currentCircle);
                tx.commit();

                sleep(100000);
            }
        }catch (Exception e){
            session.close();
            e.printStackTrace();
        }
    }

    private void updateViewers(StatelessSession session, ChannelEntity channel, CircleEntity currentCircle, CircleEntity preCircle, String[] names, String type){
        for (String name : names){
            System.out.println(name);
            if (preCircle != null) {
                Transaction tx = session.beginTransaction();
                var query = session.createQuery("from UserChannelEntity where user.name = :name and channel.id = :channel_id and type = :userType and lastCircle.id = :preCircle_id", UserChannelEntity.class);
                query.setParameter("name", name);
                query.setParameter("channel_id", channel.id);
                query.setParameter("userType", type);
                query.setParameter("preCircle_id", preCircle.id);
                var usersChannels = query.list();
                tx.commit();
                if (!usersChannels.isEmpty()) {
                    //Update
                    var userChannel = usersChannels.get(0);
                    userChannel.lastCircle = currentCircle;
                    userChannel.lastOnlineTime = new Date();
                    tx = session.beginTransaction();
                    session.update(userChannel);
                    tx.commit();
                    continue;
                }
            }

            Transaction tx = session.beginTransaction();
            var query = session.createQuery("from UserEntity where name = :name", UserEntity.class);
            query.setParameter("name", name);
            var users = query.list();
            tx.commit();

            if (users.isEmpty())
                return;

            //Insert
            var userChannel = new UserChannelEntity();
            userChannel.user = users.get(0);
            userChannel.channel = channel;
            userChannel.firstCircle = currentCircle;
            userChannel.lastCircle = currentCircle;
            userChannel.type = type;
            userChannel.firstOnlineTime = new Date();
            userChannel.lastOnlineTime = userChannel.firstOnlineTime;

            tx = session.beginTransaction();
            session.insert(userChannel);
            tx.commit();
        }
    }
}
