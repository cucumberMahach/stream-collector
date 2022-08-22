package com.streamcollector.app.grabber;

import com.streamcollector.app.grabber.goodgame.GGGrabChannelData;
import com.streamcollector.app.grabber.goodgame.GoodGameWebsocket;
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
import org.checkerframework.checker.units.qual.A;

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
    private long timeoutMs = 3500;

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
        grabGoodGame();

        log(LogStatus.Success, "Получение данных завершено");
    }

    private void grabTwitch() {
        // Twitch
        List<String> twitchChannels = new ArrayList<>();
        for (final var channel : channelsToGrab) {
            if (channel.first != Platform.Twitch)
                continue;
            twitchChannels.add(channel.second);
        }

        if (twitchChannels.isEmpty()){
            log(LogStatus.Success, "Нет Twitch каналов для обработки");
            return;
        }

        log(LogStatus.Success, String.format("Требуется обработать %d каналов Twitch", twitchChannels.size()));

        List<GrabChannelResult> results = new ArrayList<>();
        for (final var channel : twitchChannels) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getTwitchChattersUrl(channel)))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            try {
                var f = client.send(request, HttpResponse.BodyHandlers.ofString());
                GrabChannelResult result = new GrabChannelResult();
                result.platform = Platform.Twitch;
                result.channelName = channel;
                result.chattersGlobal = GrabUtil.createChattersGlobalObject(f.body());
                result.chattersGlobal.chatters.fillSetsAndConstructMaps();
                results.add(result);
            }catch (Exception e) {
                GrabChannelResult result = new GrabChannelResult();
                result.platform = Platform.Twitch;
                result.channelName = channel;
                result.setError(e);
                results.add(result);
            }
        }

        grabResults.addAll(results);
        log(LogStatus.Success, "Twitch каналы обработаны");
    }

    private void grabWASD(){
        // WASD
        List<WASDGrabChannelData> wasdClientIDResults = new ArrayList<>();
        for (final var channel : channelsToGrab) {
            if (channel.first != Platform.WASD)
                continue;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getWASDChannelIDUrl(channel.second)))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            try {
                var f = client.send(request, HttpResponse.BodyHandlers.ofString());
                var result = new WASDGrabChannelData();
                result.channelName = channel.second;
                result.channelID = GrabUtil.parseWASDChannelIDJson(f.body());
                wasdClientIDResults.add(result);
            }catch (Exception e){
                var result = new WASDGrabChannelData();
                result.channelName = channel.second;
                result.throwable = e;
                wasdClientIDResults.add(result);
            }
        }

        if (wasdClientIDResults.isEmpty()){
            log(LogStatus.Success, "Нет WASD каналов для обработки");
            return;
        }

        log(LogStatus.Success, String.format("Требуется обработать %d каналов WASD. Этап 1 - получение channel_id", wasdClientIDResults.size()));

        List<WASDGrabChannelData> wasdWithBeginError = new ArrayList<>();
        for (var ch : wasdClientIDResults){
            if (ch.channelID == null){
                wasdWithBeginError.add(ch);
            }
        }

        List<WASDGrabChannelData> wasdStreamIDResults = new ArrayList<>();
        for (final var channelData : wasdClientIDResults) {
            if (channelData.isError() || channelData.channelID == null)
                continue;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getWASDStreamIDUrl(channelData.channelID.toString())))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            try {
                var f = client.send(request, HttpResponse.BodyHandlers.ofString());
                channelData.streamID = GrabUtil.parseWASDStreamIDJson(f.body());
                wasdStreamIDResults.add(channelData);
            }catch (Exception e){
                channelData.throwable = e;
                wasdStreamIDResults.add(channelData);
            }
        }

        log(LogStatus.Success, String.format("Этап 2 - получение stream_id. Прошло %d каналов WASD", wasdStreamIDResults.size()));

        for (var ch : wasdStreamIDResults){
            if (ch.streamID == null){
                wasdWithBeginError.add(ch);
            }
        }

        List<WASDGrabChannelData> wasdDone = new ArrayList<>();

        int offset = 0;
        while (!wasdStreamIDResults.isEmpty()) {
            List<WASDGrabChannelData> wasdParticipantsResults = new ArrayList<>();
            for (final var channelData : wasdStreamIDResults) {
                if (channelData.isError() || channelData.streamID == null)
                    continue;
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GrabUtil.getWASDParticipantsUrl(channelData.streamID.toString(), offset)))
                        .timeout(Duration.ofMillis(timeoutMs))
                        .build();
                try {
                    var f = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (channelData.participants == null) {
                        channelData.participants = GrabUtil.parseWASDParticipantsJson(f.body());
                    } else {
                        channelData.participants.add(GrabUtil.parseWASDParticipantsJson(f.body()));
                    }
                    channelData.participantsTimestamp = TimeUtil.getZonedNow();
                    wasdParticipantsResults.add(channelData);
                }catch (Exception e) {
                    channelData.throwable = e;
                    wasdParticipantsResults.add(channelData);
                }

            }

            log(LogStatus.Success, String.format("Этап 3 - получение participants. Прошло %d каналов WASD. Сдвиг - %d", wasdParticipantsResults.size(), offset));
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

        log(LogStatus.Success, String.format("Успешно получено %d каналов WASD", wasdDone.size()));

        wasdDone.addAll(wasdWithBeginError);

        for (var wasdResult : wasdDone){
            grabResults.add(wasdResult.toGrabChannelResult());
        }

        log(LogStatus.Success, "WASD каналы обработаны");
    }

    private void grabTrovo() throws ExecutionException, InterruptedException, IOException {
        var usernames = channelsToGrab.stream().filter(platformStringPair -> platformStringPair.first == Platform.Trovo).toList();

        if (usernames.isEmpty()){
            log(LogStatus.Success, "Нет Trovo каналов для обработки");
            return;
        }

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
            List<TrovoGrabChannelData> trovoViewersResults = new ArrayList<>();
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
                try{
                var f = client.send(request, HttpResponse.BodyHandlers.ofString());
                    //System.out.println(s);
                    if (channelData.viewers == null){
                        channelData.viewers = GrabUtil.parseTrovoViewers(f.body());
                    }else{
                        channelData.viewers.add(GrabUtil.parseTrovoViewers(f.body()));
                    }
                    channelData.participantsTimestamp = TimeUtil.getZonedNow();
                    trovoViewersResults.add(channelData);
                }catch (Exception e) {
                    channelData.throwable = e;
                    trovoViewersResults.add(channelData);
                }
            }

            log(LogStatus.Success, String.format("Этап 3 - получение viewers. Прошло %d каналов Trovo. Сдвиг - %d", trovoViewersResults.size(), offset));
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

        log(LogStatus.Success, String.format("Успешно получено %d каналов Trovo", trovoDone.size()));

        trovoDone.addAll(trovoWithBeginErrors);

        for (var wasdResult : trovoDone){
            grabResults.add(wasdResult.toGrabChannelResult());
        }

        log(LogStatus.Success, "Trovo каналы обработаны");
    }

    private void grabGoodGame() throws ExecutionException, InterruptedException {
        List<GGGrabChannelData> ggClientIDResults = new ArrayList<>();
        for (final var channel : channelsToGrab) {
            if (channel.first != Platform.GoodGame)
                continue;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getGoodGameStreamUrl(channel.second)))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("accept", "application/vnd.goodgame.v2+json,application/hal+json,application/json")
                    .GET()
                    .build();
            try {
                var f = client.send(request, HttpResponse.BodyHandlers.ofString());

                var result = new GGGrabChannelData();
                result.channelName = channel.second;
                result.channelData = GrabUtil.parseGoodGameStream(f.body());
                result.timestamp = TimeUtil.getZonedNow();
                ggClientIDResults.add(result);
            }catch (Exception e) {
                var result = new GGGrabChannelData();
                result.channelName = channel.second;
                result.throwable = e;
                result.timestamp = TimeUtil.getZonedNow();
                ggClientIDResults.add(result);
            }
        }

        if (ggClientIDResults.isEmpty()){
            log(LogStatus.Success, "Нет GoodGame каналов для обработки");
            return;
        }

        log(LogStatus.Success, String.format("Требуется обработать %d каналов GoodGame. Этап 1 - получение channel_id", ggClientIDResults.size()));

        GoodGameWebsocket websocket = new GoodGameWebsocket();
        List<GGGrabChannelData> ggWithBeginError = new ArrayList<>();
        for (var ch : ggClientIDResults){
            if (ch.channelData == null || ch.channelData.id == null){
                ggWithBeginError.add(ch);
            }else{
                websocket.getChannels().add(ch);
            }
        }

        log(LogStatus.Success, String.format("Этап 2 - получение через websocket. Прошло %d каналов Goodgame", websocket.getChannels().size()));

        if (!websocket.getChannels().isEmpty()) {
            websocket.connectBlocking();
            long startTime = System.currentTimeMillis();
            while (!websocket.isDone()) {
                Thread.sleep(10);
                if (System.currentTimeMillis() - startTime >= timeoutMs && !websocket.isDone()) {
                    break;
                }
            }
            websocket.closeBlocking();
        }

        var results = websocket.getResults();

        log(LogStatus.Success, String.format("Успешно получено %d каналов GoodGame", results.size()));

        ArrayList<GGGrabChannelData> done = new ArrayList<>();
        for (var wsCh : websocket.getChannels()){
            var resCh = results.stream().filter(ch -> ch == wsCh).findFirst();
            if (resCh.isPresent()){
                done.add(resCh.get());
            }else{
                wsCh.throwable = new Exception("Websocket не выдал результат по этому каналу");
                done.add(wsCh);
            }
        }

        done.addAll(ggWithBeginError);

        for (var doneCh : done){
            grabResults.add(doneCh.toGrabChannelResult());
        }

        log(LogStatus.Success, "GoodGame каналы обработаны");
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
