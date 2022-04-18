package com.twitchcollector.app.grabber;

import com.twitchcollector.app.grabber.wasd.WASDGrabChannelData;
import com.twitchcollector.app.grabber.wasd.WASDGrabParticipants;
import com.twitchcollector.app.logging.LogStatus;
import com.twitchcollector.app.service.AbstractService;
import com.twitchcollector.app.util.FutureUtils;
import com.twitchcollector.app.util.Pair;
import com.twitchcollector.app.util.TimeUtil;

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

    public void startGrabAsyncHttp() throws ExecutionException, InterruptedException {
        log(LogStatus.None, String.format("Началось получение данных. Требуется загрузить информацию о %d каналах", channelsToGrab.size()));
        grabResults.clear();

        grabTwitch();
        grabWASD();

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

        for (var wasdResult : wasdDone){
            grabResults.add(wasdResult.toGrabChannelResult());
        }

        log(LogStatus.Success, "WASD каналы обработаны");
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
