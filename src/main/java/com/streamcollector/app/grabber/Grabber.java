package com.streamcollector.app.grabber;

import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.grabber.trovo.TrovoGrabChannelData;
import com.streamcollector.app.grabber.trovo.TrovoRequestUsers;
import com.streamcollector.app.grabber.trovo.TrovoRequestViewers;
import com.streamcollector.app.grabber.wasd.WASDGrabChannelData;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.service.AbstractService;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.util.FutureUtils;
import com.streamcollector.app.util.Pair;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Grabber {

    private final ArrayList<Pair<Platform, String>> channelsToGrab = new ArrayList<>();
    private final List<GrabChannelResult> grabResults = new ArrayList<>();
    private AbstractService service = null;
    private long timeoutMs = 7000;

    private static final ExecutorService pool = Executors.newFixedThreadPool(5);
    private static final HttpClient client = HttpClient.newBuilder().executor(pool).build();

    public Grabber(){

    }

    public void startGrabAsyncHttp() throws ExecutionException, InterruptedException, IOException {
        log(LogStatus.None, String.format("Началось получение данных. Требуется загрузить информацию о %d каналах", channelsToGrab.size()));
        grabResults.clear();

        grabTwitch();
        grabWASD();
        grabTrovo();

        log(LogStatus.Success, "Получение данных завершено");
    }

    private void grabTwitch() throws ExecutionException, InterruptedException {
        // Twitch
        List<CompletableFuture<GrabChannelResult>> futuresTwitch = new ArrayList<>();
        for (final var channel : channelsToGrab) {
            if (channel.first != Platform.Twitch)
                continue;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getTwitchChattersUrl(channel.second)))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            var f = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(s -> {
                GrabChannelResult result = new GrabChannelResult();
                result.platform = channel.first;
                result.channelName = channel.second;
                result.chattersGlobal = GrabUtil.createChattersGlobalObject(s);
                result.chattersGlobal.chatters.fillSetsFromArrays();
                return result;
            }).exceptionally(throwable -> {
                GrabChannelResult result = new GrabChannelResult();
                result.platform = channel.first;
                result.channelName = channel.second;
                result.setError(throwable);
                return result;
            });
            futuresTwitch.add(f);
        }
        log(LogStatus.Success, String.format("Требуется обработать %d каналов Twitch", futuresTwitch.size()));
        var futureTwitch = FutureUtils.allOf(futuresTwitch);
        grabResults.addAll(futureTwitch.get());
        log(LogStatus.Success, "Twitch каналы обработаны");
    }

    private void grabWASD() throws ExecutionException, InterruptedException {
        // WASD
        List<CompletableFuture<WASDGrabChannelData>> futuresWASDChannelID = new ArrayList<>();
        for (final var channel : channelsToGrab) {
            if (channel.first != Platform.WASD)
                continue;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getWASDChannelIDUrl(channel.second)))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            var f = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(s -> {
                var result = new WASDGrabChannelData();
                result.channelName = channel.second;
                result.channelID = GrabUtil.parseWASDChannelIDJson(s);
                return result;
            }).exceptionally(throwable -> {
                var result = new WASDGrabChannelData();
                result.channelName = channel.second;
                result.throwable = throwable;
                return result;
            });
            futuresWASDChannelID.add(f);
        }

        log(LogStatus.Success, String.format("Требуется обработать %d каналов WASD. Этап 1 - получение channel_id", futuresWASDChannelID.size()));
        var futureWASDClientID = FutureUtils.allOf(futuresWASDChannelID);
        var wasdClientIDResults = futureWASDClientID.get();

        List<WASDGrabChannelData> wasdWithBeginError = new ArrayList<>();
        for (var ch : wasdClientIDResults){
            if (ch.channelID == null){
                wasdWithBeginError.add(ch);
            }
        }

        List<CompletableFuture<WASDGrabChannelData>> futuresWASDStreamID = new ArrayList<>();
        for (final var channelData : wasdClientIDResults) {
            if (channelData.isError() || channelData.channelID == null)
                continue;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getWASDStreamIDUrl(channelData.channelID.toString())))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            var f = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(s -> {
                channelData.streamID = GrabUtil.parseWASDStreamIDJson(s);
                return channelData;
            }).exceptionally(throwable -> {
                channelData.throwable = throwable;
                return channelData;
            });
            futuresWASDStreamID.add(f);
        }

        log(LogStatus.Success, String.format("Этап 2 - получение stream_id. Прошло %d каналов WASD", futuresWASDStreamID.size()));
        var futureWASDStreamID = FutureUtils.allOf(futuresWASDStreamID);
        var wasdStreamIDResults = futureWASDStreamID.get();

        for (var ch : wasdStreamIDResults){
            if (ch.streamID == null){
                wasdWithBeginError.add(ch);
            }
        }

        List<WASDGrabChannelData> wasdDone = new ArrayList<>();

        int offset = 0;
        while (!wasdStreamIDResults.isEmpty()) {
            List<CompletableFuture<WASDGrabChannelData>> futuresWASDParticipants = new ArrayList<>();
            for (final var channelData : wasdStreamIDResults) {
                if (channelData.isError() || channelData.streamID == null)
                    continue;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GrabUtil.getWASDParticipantsUrl(channelData.streamID.toString(), offset)))
                        .timeout(Duration.ofMillis(timeoutMs))
                        .build();
                var f = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(s -> {
                    if (channelData.participants == null){
                        channelData.participants = GrabUtil.parseWASDParticipantsJson(s);
                    }else{
                        channelData.participants.add(GrabUtil.parseWASDParticipantsJson(s));
                    }
                    channelData.participantsTimestamp = TimeUtil.getZonedNow();
                    return channelData;
                }).exceptionally(throwable -> {
                    channelData.throwable = throwable;
                    return channelData;
                });
                futuresWASDParticipants.add(f);
            }

            log(LogStatus.Success, String.format("Этап 3 - получение participants. Прошло %d каналов WASD. Сдвиг - %d", futuresWASDParticipants.size(), offset));
            var futureWASDParticipants = FutureUtils.allOf(futuresWASDParticipants);
            var wasdParticipantsResults = futureWASDParticipants.get();
            var toDone = new ArrayList<WASDGrabChannelData>();

            for (var p : wasdParticipantsResults){
                if (p.isError() || p.participants == null || (p.participants.addCountHistory.isEmpty() && p.participants.countAll() < 10000) || (!p.participants.addCountHistory.isEmpty() && p.participants.addCountHistory.get(p.participants.addCountHistory.size()-1) == 0)){
                    toDone.add(p);
                }
            }

            wasdStreamIDResults.removeAll(toDone);
            wasdDone.addAll(toDone);

            offset += 10000;
        }

        wasdDone.addAll(wasdWithBeginError);

        for (var wasdResult : wasdDone){
            grabResults.add(wasdResult.toGrabChannelResult());
        }

        log(LogStatus.Success, "WASD каналы обработаны");
    }

    private void grabTrovo() throws ExecutionException, InterruptedException, IOException {
        var usernames = channelsToGrab.stream().filter(platformStringPair -> platformStringPair.first == Platform.Trovo).toList();
        ArrayList<TrovoGrabChannelData> trovoGrabs = new ArrayList<>();

        TrovoRequestUsers requestUsers = new TrovoRequestUsers();
        for (var pair : usernames){
            requestUsers.user.add(pair.second);
            var user = new TrovoGrabChannelData();
            user.channelName = pair.second;
            trovoGrabs.add(user);
        }
        log(LogStatus.Success, String.format("Требуется обработать %d каналов Trovo. Этап 1 - получение channel_id", trovoGrabs.size()));

        String requestJson = requestUsers.toJson();

        HttpRequest requestChannelId = HttpRequest.newBuilder()
                .uri(URI.create(GrabUtil.getTrovoGetUsersUrl()))
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Client-ID", Settings.instance.getPrivateSettings().trovoClientId)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        var response = client.send(requestChannelId, HttpResponse.BodyHandlers.ofString());
        var channelsUsers = GrabUtil.parseTrovoUsersJson(response.body());

        log(LogStatus.Success, String.format("Этап 2 - получение channel_id. Прошло %d каналов Trovo", channelsUsers.users.size()));

        for (var g : trovoGrabs){
            g.storeUser(channelsUsers);
        }

        ArrayList<TrovoGrabChannelData> trovoChannelIdResults = new ArrayList<>();
        ArrayList<TrovoGrabChannelData> trovoWithBeginErrors = new ArrayList<>();

        for (var g : trovoGrabs){
            if (!g.isUserError())
                trovoChannelIdResults.add(g);
            else
                trovoWithBeginErrors.add(g);
        }

        List<TrovoGrabChannelData> trovoDone = new ArrayList<>();

        int offset = 0;
        while (!trovoChannelIdResults.isEmpty()) {
            List<CompletableFuture<TrovoGrabChannelData>> futuresTrovoViewers = new ArrayList<>();
            for (final var channelData : trovoChannelIdResults) {
                if (channelData.isError())
                    continue;
                var parameters = new TrovoRequestViewers();
                parameters.limit = 0;
                parameters.cursor = offset;
                var requstString = parameters.toJson();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GrabUtil.getTrovoViewersUrl(channelData.user.channelId)))
                        .timeout(Duration.ofMillis(timeoutMs))
                        .header("Client-ID", Settings.instance.getPrivateSettings().trovoClientId)
                        .POST(HttpRequest.BodyPublishers.ofString(requstString))
                        .build();
                var f = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(s -> {
                    if (channelData.viewers == null){
                        channelData.viewers = GrabUtil.parseTrovoViewers(s);
                    }else{
                        channelData.viewers.add(GrabUtil.parseTrovoViewers(s));
                    }
                    channelData.participantsTimestamp = TimeUtil.getZonedNow();
                    return channelData;
                }).exceptionally(throwable -> {
                    channelData.throwable = throwable;
                    return channelData;
                });
                futuresTrovoViewers.add(f);
            }

            log(LogStatus.Success, String.format("Этап 3 - получение viewers. Прошло %d каналов Trovo. Сдвиг - %d", futuresTrovoViewers.size(), offset));
            var futureTrovoViewers = FutureUtils.allOf(futuresTrovoViewers);
            var trovoViewersResults = futureTrovoViewers.get();
            var toDone = new ArrayList<TrovoGrabChannelData>();

            for (var p : trovoViewersResults){
                if (p.isError() || p.viewers == null || (p.viewers.addCountHistory.isEmpty() && p.viewers.countAll() < 100000) || (!p.viewers.addCountHistory.isEmpty() && p.viewers.addCountHistory.get(p.viewers.addCountHistory.size()-1) == 0)){
                    toDone.add(p);
                }
            }

            trovoChannelIdResults.removeAll(toDone);
            trovoDone.addAll(toDone);

            offset += 100000;
        }

        trovoDone.addAll(trovoWithBeginErrors);

        for (var wasdResult : trovoDone){
            grabResults.add(wasdResult.toGrabChannelResult());
        }

        log(LogStatus.Success, "Trovo каналы обработаны");
    }

    public void log(LogStatus status, String message){
        if (service != null)
            service.writeLog(status, message);
    }

    public void setServiceToLog(AbstractService service){
        this.service = service;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public ArrayList<Pair<Platform, String>> getChannelsToGrab(){
        return channelsToGrab;
    }

    public List<GrabChannelResult> getResults(){
        return grabResults;
    }
}
