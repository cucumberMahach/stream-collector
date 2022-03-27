package util.grabber;

import util.FutureUtils;
import util.grabber.GrabChannelResult;
import util.grabber.GrabUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TwitchGrabber {

    private final ArrayList<String> channelsToGrab = new ArrayList<>();
    private List<GrabChannelResult> grabResults = new ArrayList<>();
    private int threads;
    private long timeoutMsec = 5000;

    public TwitchGrabber(int threads){
        this.threads = threads;
    }

    public TwitchGrabber(){
        this.threads = Runtime.getRuntime().availableProcessors();
    }

    /*public void startGrab() throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        List<CompletableFuture<GrabChannelResult>> futures = new ArrayList<>();
        for (final String channel : channelsToGrab) {
            CompletableFuture<GrabChannelResult> f = CompletableFuture.supplyAsync(() -> {
                GrabChannelResult result = new GrabChannelResult();
                result.channelName = channel;
                result.chattersGlobal = GrabUtil.getChattersGlobal(channel);
                return result;
            }, pool).exceptionally(throwable -> {
                GrabChannelResult result = new GrabChannelResult();
                result.channelName = channel;
                result.setError(throwable);
                return result;
            });
            futures.add(f);
        }

        var future = FutureUtils.allOf(futures);

        grabResults = future.get();
    }*/

    public void startGrabAsyncHttp() throws ExecutionException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        List<CompletableFuture<GrabChannelResult>> futures = new ArrayList<>();
        for (final String channel : channelsToGrab) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GrabUtil.getChattersUrl(channel)))
                    .timeout(Duration.ofMillis(timeoutMsec))
                    .build();
            var f = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenApply(s -> {
                GrabChannelResult result = new GrabChannelResult();
                result.channelName = channel;
                result.chattersGlobal = GrabUtil.createChattersGlobalObject(s);
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

        grabResults = future.get();
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public long getTimeoutMsec() {
        return timeoutMsec;
    }

    public void setTimeoutMsec(long timeoutMsec) {
        this.timeoutMsec = timeoutMsec;
    }

    public ArrayList<String> getChannelsToGrab(){
        return channelsToGrab;
    }

    public List<GrabChannelResult> getResults(){
        return grabResults;
    }
}
