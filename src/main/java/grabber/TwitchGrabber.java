package grabber;

import util.FutureUtils;

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
    private long timeoutMsec = 5000;

    public TwitchGrabber(){

    }

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
