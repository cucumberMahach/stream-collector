package com.twitchcollector.app.grabber;

import com.twitchcollector.app.logging.LogStatus;
import com.twitchcollector.app.service.AbstractService;
import com.twitchcollector.app.util.FutureUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwitchGrabber {

    private final ArrayList<String> channelsToGrab = new ArrayList<>();
    private final List<GrabChannelResult> grabResults = new ArrayList<>();
    private AbstractService service = null;
    private long timeoutMs = 7000;

    private static final ExecutorService pool = Executors.newFixedThreadPool(5);
    private static final HttpClient client = HttpClient.newBuilder().executor(pool).build();

    public TwitchGrabber(){

    }

    public void startGrabAsyncHttp() throws ExecutionException, InterruptedException {
        log(LogStatus.None, String.format("Началось получение данных. Требуется загрузить информацию о %d каналах", channelsToGrab.size()));
        grabResults.clear();

        List<CompletableFuture<GrabChannelResult>> futures = new ArrayList<>();

        for (final String channel : channelsToGrab) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getChattersUrl(channel)))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .build();
            var f = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(s -> {
                GrabChannelResult result = new GrabChannelResult();
                result.channelName = channel;
                result.chattersGlobal = GrabUtil.createChattersGlobalObject(s);
                result.chattersGlobal.chatters.fillSetsFromArrays();
                return result;
            }).exceptionally(throwable -> {
                GrabChannelResult result = new GrabChannelResult();
                result.channelName = channel;
                result.setError(throwable);
                return result;
            });
            futures.add(f);
        }
        var future = FutureUtils.allOf(futures);
        grabResults.addAll(future.get());
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

    public ArrayList<String> getChannelsToGrab(){
        return channelsToGrab;
    }

    public List<GrabChannelResult> getResults(){
        return grabResults;
    }
}
