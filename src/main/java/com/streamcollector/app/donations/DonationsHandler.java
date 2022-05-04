package com.streamcollector.app.donations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.streamcollector.app.donations.json.connect.*;
import com.streamcollector.app.donations.json.httpDonations.DonationData;
import com.streamcollector.app.donations.json.httpDonations.HttpDonations;
import com.streamcollector.app.donations.json.websocket.ChannelMessage;
import com.streamcollector.app.donations.json.websocket.MessageId;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.logging.SupportLogging;
import com.streamcollector.app.util.DataUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class DonationsHandler extends WebSocketClient implements SupportLogging {

    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final Gson gson = new GsonBuilder().create();
    private static final int httpsRequestsTimeoutSec = 5;

    private String bearer;

    private OAuthUser oAuthUser;
    private OnConnectResponse onConnectResponse;
    private SubscribeResponse subscribeResponse;

    public DonationsHandler(){
        super(URI.create("wss://centrifugo.donationalerts.com/connection/websocket"));
    }

    public void start() throws Exception {
        if (bearer == null){
            throw new Exception("Bearer is null");
        }

        var oauth = oAuth(bearer);
        if (oauth == null || oauth.data == null || oauth.data.id == null || oauth.data.socketConnectionToken == null || oauth.data.socketConnectionToken.isEmpty()){
            throw new Exception("oAuthUser error: " + gson.toJson(oauth));
        }
        oAuthUser = oauth.data;

        connectBlocking(5, TimeUnit.SECONDS);

        if (!isOpen()){
            throw new Exception("Соединение не открыто");
        }
    }

    private void continueStart() throws Exception {
        SubscribeRequest subscribeRequest = new SubscribeRequest();
        subscribeRequest.addAlertChannel(oAuthUser.id);
        subscribeRequest.client = onConnectResponse.result.client;
        subscribeResponse = subscribe(subscribeRequest, bearer);
        if (subscribeResponse == null || subscribeResponse.channels.isEmpty()){
            throw new Exception("SubscribeResponse error: " + gson.toJson(subscribeResponse));
        }

        boolean collectedHttp = false;
        while(!collectedHttp) {
            try {
                collectHttpDonations();
                collectedHttp = true;
            } catch (Exception e) {
                e.printStackTrace();
                Thread.sleep(2000);
            }
        }

        try{
            connectToChannels(subscribeResponse);
        }catch (Exception e){
            throw new Exception("Connect to channels error: " + DataUtil.getStackTrace(e));
        }

        log(LogStatus.Debug, "Starting done!");
    }

    private void collectHttpDonations() throws Exception {
        long currentPage = 1;

        HttpDonations httpDonations;
        while (true){
            log(LogStatus.Debug, String.format("CurrentPage: %d", currentPage));
            httpDonations = getDonationsByHttp(bearer, currentPage);

            if (httpDonations == null)
                throw new Exception("HttpDonation is null");
            for (var donat : httpDonations.data){
                if (isDonationActual(donat)){
                    onNewDonation(donat);
                }else{
                    return;
                }
            }
            currentPage++;

            if (currentPage == httpDonations.meta.lastPage){
                return;
            }
            Thread.sleep(500);
        }
    }

    public void setBearer(String bearer) {
        this.bearer = bearer;
    }

    protected abstract void onNewDonation(DonationData donation);

    protected abstract boolean isDonationActual(DonationData donation);

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        if (oAuthUser == null){
            close();
            return;
        }
        String msg = String.format("""
                {
                    "params": {
                        "token": "%s"
                    },
                    "id": %d
                }
                """, oAuthUser.socketConnectionToken, 1);
        log(LogStatus.Debug, msg);
        send(msg);
    }

    @Override
    public void onMessage(String s) {
        log(LogStatus.Debug, "OnMessage = " + s);
        var messageId = gson.fromJson(s, MessageId.class);
        if (messageId.id != null && messageId.id == 1){
            onConnectResponse = gson.fromJson(s, OnConnectResponse.class);
            try {
                continueStart();
            } catch (Exception e) {
                e.printStackTrace();
                close();
            }
        }else{
            var channelMessage = gson.fromJson(s, ChannelMessage.class);
            if (channelMessage != null && channelMessage.isCorrect()){
                if (isDonationActual(channelMessage.result.data.data)) {
                    onNewDonation(channelMessage.result.data.data);
                }
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        log(LogStatus.Debug, "OnClose = " + s);
    }

    @Override
    public void onError(Exception e) {
        log(LogStatus.Debug, "OnError = " + DataUtil.getStackTrace(e));
    }

    private void connectToChannels(SubscribeResponse subscribeResponse){
        for (var ch : subscribeResponse.channels) {
            String msg = String.format("""
                    {
                        "params": {
                            "channel": "%s",
                            "token": "%s"
                        },
                        "method": 1,
                        "id": %d
                    }
                    """, ch.channel, ch.token, 2);
            send(msg);
        }
    }

    private OAuthUserHolder oAuth(String bearer) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.donationalerts.com/api/v1/user/oauth"))
                .header("Authorization", String.format("Bearer %s", bearer))
                .timeout(Duration.ofSeconds(httpsRequestsTimeoutSec))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var result = gson.fromJson(response.body(), OAuthUserHolder.class);
        log(LogStatus.Debug, "Response oauth: " + response.body());
        return result;
    }

    private SubscribeResponse subscribe(SubscribeRequest subscribeRequest, String bearer) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.donationalerts.com/api/v1/centrifuge/subscribe"))
                .header("Authorization", String.format("Bearer %s", bearer))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subscribeRequest)))
                .timeout(Duration.ofSeconds(httpsRequestsTimeoutSec))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var result = gson.fromJson(response.body(), SubscribeResponse.class);
        log(LogStatus.Debug, "Response subscribe: " + response.body());
        return result;
    }

    private HttpDonations getDonationsByHttp(String bearer) throws IOException, InterruptedException {
        return getDonationsByHttp(bearer, null);
    }

    private HttpDonations getDonationsByHttp(String bearer, Long page) throws IOException, InterruptedException {
        String url = "https://www.donationalerts.com/api/v1/alerts/donations";
        if (page != null){
            url += String.format("?page=%d", page);
        }

        var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Authorization", String.format("Bearer %s", bearer))
                .timeout(Duration.ofSeconds(httpsRequestsTimeoutSec))
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.body() == null || response.body().isEmpty()){
            log(LogStatus.Debug,"Http donations: null");
            return null;
        }
        var result = gson.fromJson(response.body(), HttpDonations.class);
        log(LogStatus.Debug,"Http donations: " + response.body());
        return result;
    }
}
