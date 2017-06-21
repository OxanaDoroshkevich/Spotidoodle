package com.spotidoodle.team13.spotidoodle;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.method.SingleLineTransformationMethod;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Player;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Result;
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

        }
        this.api = new SpotifyApi();
        this.api.setAccessToken(this.ACCSSES_TOKEN);
        spotify = api.getService();
        final TextView title = (TextView) findViewById(R.id.playlistTitle);
        title.setText(this.playlistTitle);
        Button savePlaylist = (Button) findViewById(R.id.saveButton);
        savePlaylist.setBackgroundResource(R.drawable.circle);
        savePlaylist.setOnClickListener(onClickListener);

        if (intent.getFlags() == 0) {
            spotify.getPlaylistTracks(userID, playlist, new Callback<Pager<PlaylistTrack>>() {
                @Override
                public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
                    List<PlaylistTrack> playlistTracks = playlistTrackPager.items;
                    final TableLayout playlistTable = (TableLayout) findViewById(R.id.playlistTable);
                    for( final PlaylistTrack track : playlistTracks){
                        Button song = new Button(SortedPlaylists.this);
                        setButtonLayout(song);
                        final TextView value = new TextView((SortedPlaylists.this));
                        song.setText(track.track.name);
                        spotify.getTrackAudioFeatures(track.track.id, new Callback<AudioFeaturesTrack>() {
                            @Override
                            public void success(AudioFeaturesTrack audioFeaturesTrack, Response response) {
                                value.setText(algorithm + " value " + String.valueOf(getSortingAlgorithm(algorithm, audioFeaturesTrack)));
                                setTextLayout(value);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                error.printStackTrace();
                            }
                        });
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
                @Override
                public void failure(RetrofitError error) {
                    title.setText("no songs found");
                    error.printStackTrace();
                }
            });
        } else {
            this.unsortedTracks = new TreeMap(Collections.reverseOrder());
            final TableLayout playlistTable = (TableLayout) findViewById(R.id.playlistTable);
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
        final Button sortPlaylist = (Button) findViewById(R.id.sortButton);
        sortPlaylist.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.sortButton:
                    finish();
                    startActivity(getIntent());
                    break;
                case R.id.saveButton:
                    Map map = unsortedTracks;
                    spotify.createPlaylist(userID, createObjectMap(unsortedTracks), new Callback<Playlist>() {
                        @Override
                        public void success(Playlist playlist, Response response) {
                            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%" + playlist.name);
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
                        }
                    });
            }
        }
    };

    private Map createObjectMap(TreeMap unsortedTracks) {
        Map map = new TreeMap();
        int index = 0;
        for (Object track : unsortedTracks.entrySet()) {
            map.put(index, track);
            index++;
        }
        return map;
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
