package com.kassette;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.playback.Config;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Connectivity;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by saurabhsharma on 2/23/15.
 */
public class SpotifyManager implements PlayerNotificationCallback, ConnectionStateCallback {

    private static final String TAG = "SpotifyManager";
    private boolean PLAYER_SETUP_COMPLETE = false;

    private static final String CLIENT_ID = "f3cc9074ef5f44caacaa615c1fdf1d4a";
    private static final String REDIRECT_URI = "kassette-login://callback";
    public static final int REQUEST_CODE = 1337;
    private Player mPlayer;
    private PlayerState mCurrentPlayerState = new PlayerState();
    //TODO: fix later mo
    private MediaManager mediaManager;



    Context appContext;
    Activity mainActivity;
    public boolean isPaused = false;

    public SpotifyManager(Context context, Activity activity) {
        appContext = context;
        mainActivity = activity;
    }

    SpotifyApi api = new SpotifyApi();

    SpotifyService spotify = api.getService();


    public void openLoginWindow() {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"user-read-private"})
                .build();
        AuthenticationClient.openLoginActivity(mainActivity, REQUEST_CODE, request);
    }

    private void logStatus(String msg) {
        Log.e("MainActivity", msg);
    }

    public void spotifyResponse(int requestCode, int resultCode, Intent intent) {
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.e("MainActivity", "Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.e("MainActivity", "Auth result: " + response.getType());
            }
        }
    }

    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        // Once we have obtained an authorization token, we can proceed with creating a Player.
        logStatus("Got authentication token");

        if (mPlayer == null) {
            Config playerConfig = new Config(appContext, authResponse.getAccessToken(), CLIENT_ID);
            // Since the Player is a static singleton owned by the Spotify class, we pass "this" as
            // the second argument in order to refcount it properly. Note that the method
            // Spotify.destroyPlayer() also takes an Object argument, which must be the same as the
            // one passed in here. If you pass different instances to Spotify.getPlayer() and
            // Spotify.destroyPlayer(), that will definitely result in resource leaks.
            mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                @Override
                public void onInitialized(Player player) {
                    logStatus("-- Player initialized --");
                    mPlayer.setConnectivityStatus(getNetworkConnectivity(appContext));
                    mPlayer.addPlayerNotificationCallback(SpotifyManager.this);
                    mPlayer.addConnectionStateCallback(SpotifyManager.this);
                }

                @Override
                public void onError(Throwable error) {
                    logStatus("Error in initialization: " + error.getMessage());
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
        }
    }

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    public void PlaySong(String uri) throws Exception {
        if (PLAYER_SETUP_COMPLETE) {
            mPlayer.play(uri);
        } else {
            throw new Exception("PLAYER SETUP IS NOT COMPLETE. Did you call openLoginWindow?");
        }
    }

    public void pauseSong() throws Exception{
        if (PLAYER_SETUP_COMPLETE) {
            mPlayer.pause();
            isPaused = true;
        } else {
            throw new Exception("PLAYER SETUP IS NOT COMPLETE. Did you call openLoginWindow?");
        }
    }

    public void resumeSong() throws Exception{
        if (PLAYER_SETUP_COMPLETE) {
            mPlayer.resume();
        } else {
            throw new Exception("PLAYER SETUP IS NOT COMPLETE. Did you call openLoginWindow?");
        }
    }

    public void clear() throws Exception{
        if (PLAYER_SETUP_COMPLETE) {
            mPlayer.clearQueue();
        } else {
            throw new Exception("PLAYER SETUP IS NOT COMPLETE. Did you call openLoginWindow?");
        }
    }




    @Override
    public void onLoggedIn() {
        logStatus("User logged in");
        PLAYER_SETUP_COMPLETE = true;
    }

    @Override
    public void onLoggedOut() {
        logStatus("User logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        logStatus("User logged failed");
    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {
        logStatus(s);
    }

    public void setMediaManager(MediaManager mediaManager){
        this.mediaManager = mediaManager;
    }
    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        if(eventType == EventType.TRACK_END && mediaManager != null){
            Log.d(TAG, "onCompleteSpotifySong");
            mediaManager.onCompleteSpotifySong();
        }
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        logStatus(s);
    }
}
