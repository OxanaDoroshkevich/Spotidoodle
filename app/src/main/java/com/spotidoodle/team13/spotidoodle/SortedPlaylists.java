package com.spotidoodle.team13.spotidoodle;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Oxana on 19.06.2017.
 */

public class SortedPlaylists  extends AppCompatActivity {

    private String playlist;
    private String playlistUri;
    private SpotifyService spotify;
    private SpotifyApi api;
    private String ACCSSES_TOKEN;
    private String userID;
    private String playlistTitle;
    private String algorithm;
    private TreeMap <Float, PlaylistTrack> unsortedTracks;
    private boolean isIncreasing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sorted_playlist);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            this.playlist = bundle.getString("playlist");
            this.playlistUri =  bundle.get("playlistUri").toString();
            this.ACCSSES_TOKEN = bundle.getString("accessToken");
            this.userID = bundle.getString("userID");
            this.playlistTitle = bundle.getString("playlistTitle");
            this.algorithm = bundle.getString("algorithm");
            this.isIncreasing = bundle.getBoolean("isIncreasing");

        }
        System.out.println("THE BOOL VALUE AFTER THE ACTIVITY STARTED : " + isIncreasing);
        this.api = new SpotifyApi();
        this.api.setAccessToken(this.ACCSSES_TOKEN);
        spotify = api.getService();
        final TextView title = (TextView) findViewById(R.id.playlistTitle);
        title.setText(this.playlistTitle);
        Button savePlaylist = (Button) findViewById(R.id.saveButton);
        savePlaylist.setBackgroundResource(R.drawable.circle);
        savePlaylist.setOnClickListener(onClickListener);
        final ImageButton sortPlaylist = (ImageButton) findViewById(R.id.sortButton);

        if (!this.isIncreasing) {
            this.unsortedTracks = new TreeMap(Collections.reverseOrder());
            final TableLayout playlistTable = (TableLayout) findViewById(R.id.playlistTable);
            displaySortedTracksInTable(playlistTable, title);
            sortPlaylist.setImageResource(R.drawable.arrow_up_small);
        } else {
            this.unsortedTracks = new TreeMap();
            final TableLayout playlistTable = (TableLayout) findViewById(R.id.playlistTable);
            displaySortedTracksInTable(playlistTable, title);
            sortPlaylist.setImageResource(R.drawable.arrow_down_small);
        }

        sortPlaylist.setOnClickListener(onClickListener);
    }

    private void displaySortedTracksInTable(final TableLayout playlistTable, final TextView title) {
        spotify.getPlaylistTracks(userID, playlist, new Callback<Pager<PlaylistTrack>>() {
            @Override
            public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                final List<PlaylistTrack> playlistTracks = playlistTrackPager.items;
                for (final PlaylistTrack track : playlistTracks) {
                    spotify.getTrackAudioFeatures(track.track.id, new Callback<AudioFeaturesTrack>() {
                        @Override
                        public void success(AudioFeaturesTrack audioFeaturesTrack, Response response) {
                            unsortedTracks.put(getSortingAlgorithm(algorithm, audioFeaturesTrack), track);
                            if (playlistTracks.size() == unsortedTracks.size()) {
                                for (Map.Entry<Float, PlaylistTrack> track : unsortedTracks.entrySet()) {
                                    Button song = new Button(SortedPlaylists.this);
                                    final TextView value = new TextView((SortedPlaylists.this));
                                    song.setText(track.getValue().track.name);
                                    setButtonLayout(song);
                                    value.setText(algorithm + " value " +track.getKey().toString());
                                    setTextLayout(value);
                                    TableRow row = new TableRow(SortedPlaylists.this);
                                    row.setBackgroundResource(R.drawable.rowlayout);
                                    GridLayout grid = new GridLayout(SortedPlaylists.this);
                                    TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                                    grid.addView(song);
                                    grid.addView(value);
                                    row.addView(grid, rowLayout);
                                    playlistTable.addView(row);
                                }
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            error.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void failure(RetrofitError error) {
                title.setText("no songs found");
                error.printStackTrace();
            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.sortButton:
                    isIncreasing = !isIncreasing;
                    System.out.println("BOOLEAN VALUE :   " + isIncreasing);
                    Intent intent = getIntent();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isIncreasing", isIncreasing);
                    intent.putExtras(bundle);
                    finish();
                    startActivity(intent);
                    break;
                case R.id.saveButton:
                    createPlaylistUsingBodyMap();
            }
        }
    };

    private void createPlaylistUsingBodyMap() {
        final String owner = userID;
        final String name = this.playlistTitle + " sorted with decreasing " + this.algorithm;
        final boolean isPublic = true;
        final Map<String, Object> options = new HashMap();
        options.put("name", name);
        options.put("public", isPublic);
        spotify.createPlaylist(owner, options, new SpotifyCallback<Playlist>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                spotifyError.printStackTrace();
            }

            @Override
            public void success(final Playlist playlist, Response response) {
                if (playlist != null) {
                    addTracksToNewPlaylist(playlist.id, name);
                }
            }
        });
    }

    private void addTracksToNewPlaylist(String playlistID, String name) {
        final int position = 0;
        final String playlistName = name;
        final Map<String, Object> options = new HashMap<>();
        final List<String> trackUris = new ArrayList();
        for (Map.Entry<Float, PlaylistTrack> track : unsortedTracks.entrySet()) {
            trackUris.add(track.getValue().track.uri);
        }
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
                CharSequence text = "Created new playlist " + playlistName;
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }

    private Float getSortingAlgorithm(String algorithm, AudioFeaturesTrack analyser) {
        switch (algorithm) {
            case "danceability":
                return analyser.danceability;
            case "energy":
                return analyser.energy;
            case "loudness":
                return analyser.loudness;
            case "tempo":
                return analyser.tempo;
        }
        return Float.valueOf("0.0");
    }

    private void setButtonLayout(Button button){
        //button.setBackgroundResource(R.drawable.buttonstyling); blue radiant background
        button.setAlpha((float) 0.8);
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        button.setWidth((width/5) * 3);
    }

    private void setTextLayout(TextView text) {
        text.setBackgroundResource(R.drawable.textstyling);
        text.setAlpha((float) 0.7);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        text.setWidth((width/5) * 2);
    }
}
