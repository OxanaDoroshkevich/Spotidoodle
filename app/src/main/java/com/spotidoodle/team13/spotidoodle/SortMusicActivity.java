package com.spotidoodle.team13.spotidoodle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.spotify.sdk.android.player.PlaybackState;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.TrackToRemove;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Oxana on 01.06.2017.
 */

public class SortMusicActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback  {

    private Player mPlayer;
    private String CLIENT_ID;
    private String playlist;
    private String playlistUri;
    private SpotifyService spotify;
    private SpotifyApi api;
    private String ACCSSES_TOKEN;
    private String userID;
    private String playlistID;
    private String toSavePlaylistID;
    private String playlistTitle;
    private int REQUEST_CODE;
    private static final String REDIRECT_URI = "http://spotidoodle2.com/callback/";

    /**
     * the spotify operation callback
     */
    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            System.out.println("OK!");
        }

        @Override
        public void onError(Error error) {
            System.out.println("ERROR:" + error);
        }
    };

    /**
     * called when on activity start
     * gets the intent and the bundle with extras and gets again the spotify authentication request as
     * in MainActivity. Defines the buttons and functionality
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_music);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            this.CLIENT_ID = bundle.getString("clientID");
            this.REQUEST_CODE = bundle.getInt("requestCode");
            this.playlist = bundle.getString("playlist");
            this.playlistUri =  bundle.get("playlistUri").toString();
            this.ACCSSES_TOKEN = bundle.getString("accessToken");
            this.userID = bundle.getString("userID");
            this.playlistID = bundle.getString("playlistID");
            this.playlistTitle = bundle.getString("playlistTitle");
        }
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        getButtonContent();
    }

    private void getButtonContent() {
        final ImageButton fave = (ImageButton) findViewById(R.id.imageButtonFave);
        final ImageButton seek = (ImageButton) findViewById(R.id.imageButtonSeek);
        final ImageButton shuffle = (ImageButton) findViewById(R.id.imageButtonShuffle);
        final ImageButton skip = (ImageButton) findViewById(R.id.imageButtonSkip);
        final TextView playlistText = (TextView) findViewById(R.id.playlistName);
        setButtonLayout(fave);
        setButtonLayout(shuffle);
        setButtonLayout(seek);
        setButtonLayout(skip);
        playlistText.setText(playlistTitle);
        fave.setOnClickListener(onClickListener);
        seek.setOnClickListener(onClickListener);
        shuffle.setOnClickListener(onClickListener);
        skip.setOnClickListener(onClickListener);
    }

    /**
     * called
     * Get a result depending on if the Authentication was successful or not
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
     * called when Authentication process is completed
     * initializes the player object, the Spotify api and the spotify service
     * @param authResponse
     */
    private void onAuthenticationComplete(AuthenticationResponse authResponse) {
        this.api = new SpotifyApi();
        this.api.setAccessToken(this.ACCSSES_TOKEN);
        spotify = api.getService();
        if (mPlayer == null) {
            Config playerConfig = new Config(getApplicationContext(), authResponse.getAccessToken(), CLIENT_ID);
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    mPlayer = spotifyPlayer;
                    mPlayer.addConnectionStateCallback(SortMusicActivity.this);
                    mPlayer.addNotificationCallback(SortMusicActivity.this);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                }
            });
        } else {
            mPlayer.login(authResponse.getAccessToken());
        }
        if (playlistUri != null && mOperationCallback != null) {
            mPlayer.playUri(mOperationCallback, playlistUri, 0, 0);
            mPlayer.setRepeat(mOperationCallback, true);
        }
    }


    /**
     * called when a button is clicked in the activity
     * calls function depending on which button has been clicked
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.imageButtonShuffle:
                    mPlayer.setShuffle(mOperationCallback, true);
                    break;
                case R.id.imageButtonFave:
                    spotify.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
                        @Override
                        public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                            toSavePlaylistID = playlistSimplePager.items.get(0).id;
                            addTracksToNewPlaylist(toSavePlaylistID, mPlayer.getMetadata().currentTrack.uri);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            error.printStackTrace();
                        }
                    });
                    break;
                case R.id.imageButtonSeek:
                    mPlayer.seekToPosition(mOperationCallback, 60000);
                    break;
                case R.id.imageButtonSkip:
                    mPlayer.skipToNext(mOperationCallback);
                    setTrackCredentials();
                    break;
            }
        }
    };

    /**
     * displays the track information like:
     * name, artist, album and the cover of the song
     */
    private void setTrackCredentials() {
        TextView trackName = (TextView) findViewById(R.id.songName);
        TextView artistName = (TextView) findViewById(R.id.artistName);
        TextView albumName = (TextView) findViewById(R.id.albumName);
        ImageView albumCover = (ImageView) findViewById(R.id.albumImage);
        trackName.setText(mPlayer.getMetadata().currentTrack.name);
        artistName.setText(mPlayer.getMetadata().currentTrack.artistName);
        albumName.setText(mPlayer.getMetadata().currentTrack.albumName);
        String albumImage = mPlayer.getMetadata().currentTrack.albumCoverWebUrl;
        Picasso.with(getApplicationContext()).load(albumImage).into(albumCover);
    }

    /**
     * called when the button imageButtonFave has been clicked
     * creates a HashMap and a List and calls the addTracksToPlaylist of spotify service
     * @param playlistID
     * @param trackUri
     */
    private void addTracksToNewPlaylist(String playlistID, String trackUri) {
        System.out.println("SPOTIFYTRACK : " + trackUri);
        final int position = 0;
        //final String playlistName = name;
        final Map<String, Object> options = new HashMap();
        final List<String> trackUris = Arrays.asList(trackUri);
        options.put("uris", trackUris);
        final Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("position", String.valueOf(position));
        spotify.addTracksToPlaylist(userID, playlistID, queryParameters, options, new SpotifyCallback<Pager<PlaylistTrack>>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                spotifyError.printStackTrace();
            }

            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                Context context = getApplicationContext();
                CharSequence text = "Added " + mPlayer.getMetadata().currentTrack.name + " to playlist";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    /**
     * new button layout for all buttons in the grid layout
     * @param button
     */
    private void setButtonLayout(ImageButton button){
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        button.setMinimumWidth((width/2));
        button.setMinimumHeight((height - findViewById(R.id.header).getHeight() - findViewById(R.id.info_bg).getHeight()) / 4);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            this.playlist = bundle.getString("playlist");
            this.playlistUri =  bundle.get("playlistUri").toString();
            this.ACCSSES_TOKEN = bundle.getString("accessToken");
            this.userID = bundle.getString("userID");
            this.playlistTitle = bundle.getString("playlistTitle");
        }
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mPlayer.pause(mOperationCallback);
    }

    /**
     * called when spotofy player is not used anymore
     */
    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    /**
     *if player functionality is changing, like skip to next song
     *if player event is changing the song the setTrackCredentials method is called again
     * @param playerEvent
     */
    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        switch (playerEvent) {
            case kSpPlaybackNotifyPlay:
                setTrackCredentials();
                break;
            case kSpPlaybackNotifyNext:
                setTrackCredentials();
                break;
            case kSpPlaybackNotifyTrackChanged:
                setTrackCredentials();
                break;
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

    }

    /**
     * if user can't be logged in
     * @param
     */
    @Override
    public void onLoginFailed(Error error) {

    }

    /**
     * if an error occurred
     */
    @Override
    public void onTemporaryError() {

    }

    /**
     * get the connection message
     * @param s
     */
    @Override
    public void onConnectionMessage(String s) {

    }
}
