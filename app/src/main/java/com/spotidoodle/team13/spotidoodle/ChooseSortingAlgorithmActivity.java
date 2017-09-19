package com.spotidoodle.team13.spotidoodle;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;

/**
 * Created by Oxana on 19.06.2017.
 */

public class ChooseSortingAlgorithmActivity extends AppCompatActivity {

    private String playlist;
    private String playlistUri;
    private String ACCSSES_TOKEN;
    private String userID;
    private String playlistTitle;
    private String ownerID;

    /**
     * on activity start this method is called
     * gets the intent and the bundle with extras which are sored in the intent
     * defines the buttons
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_algorithm);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            this.playlist = bundle.getString("playlist");
            this.playlistUri =  bundle.get("playlistUri").toString();
            this.ACCSSES_TOKEN = bundle.getString("accessToken");
            this.userID = bundle.getString("userID");
            this.playlistTitle = bundle.getString("playlistTitle");
            this.ownerID = bundle.getString("ownerID");
        }

        final Button danceability = (Button) findViewById(R.id.algorithmDance);
        final Button bpm = (Button) findViewById(R.id.algorithmBPM);
        final Button energy = (Button) findViewById(R.id.algorithmEnergy);
        final Button loudness = (Button) findViewById(R.id.algorithmLoudness);
        danceability.setOnClickListener(onClickListener);
        bpm.setOnClickListener(onClickListener);
        energy.setOnClickListener(onClickListener);
        loudness.setOnClickListener(onClickListener);
    }

    /**
     * called on button clicked
     * created a new intent with a bundle and parameters the
     * next activity needs and starts the next activity
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            Intent intent;
            Bundle bundle;
            switch(v.getId()){
                case R.id.algorithmDance:
                    intent = new Intent(ChooseSortingAlgorithmActivity.this, SortedPlaylists.class);
                    bundle = new Bundle();
                    bundle.putString("algorithm", "danceability");
                    setBundleParameters(bundle);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case R.id.algorithmBPM:
                    intent = new Intent(ChooseSortingAlgorithmActivity.this, SortedPlaylists.class);
                    bundle = new Bundle();
                    bundle.putString("algorithm", "tempo");
                    setBundleParameters(bundle);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case R.id.algorithmEnergy:
                    intent = new Intent(ChooseSortingAlgorithmActivity.this, SortedPlaylists.class);
                    bundle = new Bundle();
                    bundle.putString("algorithm", "energy");
                    setBundleParameters(bundle);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case R.id.algorithmLoudness:
                    intent = new Intent(ChooseSortingAlgorithmActivity.this, SortedPlaylists.class);
                    bundle = new Bundle();
                    bundle.putString("algorithm", "loudness");
                    setBundleParameters(bundle);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
            }
        }
    };

    /**
     * defines the parameters a bundle needs, for all intent in the class
     * called on every button click
     * @param bundle
     */
    private void setBundleParameters(Bundle bundle) {
        bundle.putString("playlist", playlist);
        bundle.putString("playlistUri", playlistUri);
        bundle.putString("accessToken", ACCSSES_TOKEN);
        bundle.putString("userID", userID);
        bundle.putString("playlistTitle", playlistTitle);
        bundle.putString("ownerID", ownerID);
        bundle.putBoolean("isIncreasing", false);
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
            this.ownerID = bundle.getString("ownerID");
        }
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
    }
}
