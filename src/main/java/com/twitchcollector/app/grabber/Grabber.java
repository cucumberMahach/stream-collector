package com.twitchcollector.app.grabber;

import com.twitchcollector.app.grabber.wasd.WASDGrabChannelID;
import com.twitchcollector.app.grabber.wasd.WASDGrabParticipants;
import com.twitchcollector.app.grabber.wasd.WASDGrabStreamID;
import com.twitchcollector.app.logging.LogStatus;
import com.twitchcollector.app.service.AbstractService;
import com.twitchcollector.app.util.FutureUtils;
import com.twitchcollector.app.util.Pair;

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

        // WASD
        List<CompletableFuture<WASDGrabChannelID>> futuresWASDChannelID = new ArrayList<>();
        List<CompletableFuture<WASDGrabStreamID>> futuresWASDStreamID = new ArrayList<>();
        List<CompletableFuture<WASDGrabParticipants>> futuresWASDParticipants = new ArrayList<>();

        var futureTwitch = FutureUtils.allOf(futuresTwitch);
        grabResults.addAll(futureTwitch.get());
        log(LogStatus.Success, "Получение данных завершено");
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
