package com.spotidoodle.team13.spotidoodle;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "9f703a39b15a4241b08dcea6685e5f50";
    private static final String REDIRECT_URI = "http://spotidoodle2.com/callback/";
    private static final int REQUEST_CODE = 1337;
    private Pager<PlaylistSimple> myPlaylists;
    private UserPrivate user;
    private SpotifyApi api;
    private String ACCSSES_TOKEN;
    private String userID;
    private Player mPlayer;

    /**
     * first method calles on activity start, loads the drawable resources and layout
     * generates the authentication builder with the set of scopes (also permissions) that the app gets
     * then opens the spotify login activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private",
                                        "playlist-read-private",
                                        "playlist-modify-public",
                                        "playlist-modify-private",
                                        "playlist-read-collaborative",
                                        "user-library-modify",
                                        "user-library-read",
                                        "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    /**
     * method from the connection state callback, checks if the authentication was successfully
     * if response gets token then start the onAuthenticationComplete method
     * if not print the error stacktrace
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    onAuthenticationComplete(response);
                    break;
                case ERROR:
                    Log.d("Auth error: ", response.getError());
                    break;
                default:
                    Log.d("Auth error: ", response.getType().toString());
            }
        }
    }

    /**
     * checks if authentication was successfully completed
     * here we create the spotify api and the spotify service instance to get the user private data
     * getMe is a spotify service method and always needs a callback
     * here we create the player instance
     * @param authResponse
     */
    private void onAuthenticationComplete(final AuthenticationResponse authResponse) {
        this.api = new SpotifyApi();
        this.api.setAccessToken(authResponse.getAccessToken());
        this.ACCSSES_TOKEN = authResponse.getAccessToken();
        final TextView userName = (TextView) findViewById(R.id.textView);
        final ImageView userImage = (ImageView) findViewById(R.id.imageView2);
        final SpotifyService spotify = api.getService();
        spotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                user = userPrivate;
                if (user.display_name != null) {
                    userName.setText(user.display_name.toString());
                } else {
                    userName.setText(user.id.toString());
                }
                if (user.images.size() > 0) {
                    String imageURL = user.images.get(0).url;
                    Picasso.with(getApplicationContext()).load(imageURL).transform( new CircleTransform()).into(userImage);
                }
                userID = userPrivate.id;
            }

            @Override
            public void failure(RetrofitError error) {
                userName.setText("No Spotify user name found");
                System.out.println(error.getStackTrace());
            }
        });
        this.getUserPlaylists(spotify);
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    mPlayer = spotifyPlayer;
                    mPlayer.addConnectionStateCallback(MainActivity.this);
                    mPlayer.addNotificationCallback(MainActivity.this);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
        }
    }

    /**
     * queue the user private, public and collaborative playlists
     * uses the spotify web api and the spotify service to queue the playlists
     * @param spotify
     */
    private void getUserPlaylists(final SpotifyService spotify) {
        HashMap<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, 0);
        options.put(SpotifyService.LIMIT, 50);
        spotify.getMyPlaylists(options, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                myPlaylists = playlistSimplePager;
                for (int i = 0; i < myPlaylists.items.size(); i++){
                    final int iterator = i;
                    Button playlistButton = new Button(MainActivity.this);
                    playlistButton.setText(myPlaylists.items.get(i).name);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        playlistButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    }
                    playlistButton.setId(iterator);
                    TableLayout table = (TableLayout) findViewById(R.id.buttonLayout);
                    playlistButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, ChooseTaskActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("playlist", myPlaylists.items.get(iterator).id);
                            bundle.putString("playlistUri", myPlaylists.items.get(iterator).uri);
                            bundle.putString("ownerID", myPlaylists.items.get(iterator).owner.id);
                            bundle.putString("clientID", CLIENT_ID);
                            bundle.putInt("requestCode", REQUEST_CODE);
                            bundle.putString("accessToken", ACCSSES_TOKEN);
                            bundle.putString("userID", userID);
                            bundle.putString("playlistTitle", myPlaylists.items.get(iterator).name);
                            bundle.putString("playlistID", myPlaylists.items.get(iterator).id);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    playlistButton.setBackgroundColor(4);
                    playlistButton.setGravity(Gravity.CENTER_HORIZONTAL);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    playlistButton.setLayoutParams(params);
                    TableRow row = new TableRow(MainActivity.this);
                    TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    row.addView(playlistButton, rowLayout);
                    TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    table.addView(row, tableLayout);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });
        /*spotify.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                myPlaylists = playlistSimplePager;
                for (int i = 0; i < myPlaylists.items.size(); i++){
                    final int iterator = i;
                    Button playlistButton = new Button(MainActivity.this);
                    playlistButton.setText(myPlaylists.items.get(i).name);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        playlistButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                    }
                    playlistButton.setId(iterator);
                    TableLayout table = (TableLayout) findViewById(R.id.buttonLayout);
                    playlistButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(MainActivity.this, ChooseTaskActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("playlist", myPlaylists.items.get(iterator).id);
                            bundle.putString("playlistUri", myPlaylists.items.get(iterator).uri);
                            bundle.putString("ownerID", myPlaylists.items.get(iterator).owner.id);
                            bundle.putString("clientID", CLIENT_ID);
                            bundle.putInt("requestCode", REQUEST_CODE);
                            bundle.putString("accessToken", ACCSSES_TOKEN);
                            bundle.putString("userID", userID);
                            bundle.putString("playlistTitle", myPlaylists.items.get(iterator).name);
                            bundle.putString("playlistID", myPlaylists.items.get(iterator).id);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    playlistButton.setBackgroundColor(4);
                    playlistButton.setGravity(Gravity.CENTER_HORIZONTAL);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    playlistButton.setLayoutParams(params);
                    TableRow row = new TableRow(MainActivity.this);
                    TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    row.addView(playlistButton, rowLayout);
                    TableLayout.LayoutParams tableLayout = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                    table.addView(row, tableLayout);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });*/
    }

    /**
     * if player is not used anymore
     */
    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    /**
     *if player functionality is changing, like skip to next song
     * @param playerEvent
     */
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        switch (playerEvent) {
            default:
                break;
        }
    }

    /**
     * if player has an error
     * @param error
     */
    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            default:
                break;
        }
    }

    /**
     * if user is logged in and player is receiving data
     */
    @Override
    public void onLoggedIn() {
        mPlayer.addNotificationCallback(new Player.NotificationCallback() {
            @Override
            public void onPlaybackEvent(PlayerEvent playerEvent) {
                if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackDelivered) {
                    System.out.println("Delivered");
                }
            }

            @Override
            public void onPlaybackError(Error error) {
            }
        });
    }

    /**
     * if user is logged out
     */
    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    /**
     * if user can't be logged in
     * @param e
     */
    @Override
    public void onLoginFailed(Error e) {
        Log.d("MainActivity", "Login failed");
    }

    /**
     * if an error occurred
     */
    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    /**
     * get the connection message
     * @param message
     */
    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }
}