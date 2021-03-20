package com.neo.aimusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class SmartPlayerActivity extends AppCompatActivity {
    private RelativeLayout parentRelativeLayout;
    // speech recog and it's intent
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private ImageView pausePlayBtn, nextBtn, previousBtn;
    private TextView songNameTxt;
    private ImageView imageView;
    private RelativeLayout lowerRelativeLayout;
    private Button voiceEnableBtn;

    private String mode = "ON";   // var to track when the voiceEnabled feature is on or off.

    // obj to play media in android
    private MediaPlayer myMediaPlayer;
    private int position;
    private ArrayList<File> mySongs;
    private String mSongName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_player);
        checkVoiceCommandPermission();

        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);
        pausePlayBtn = findViewById(R.id.play_pause_btn);
        nextBtn = findViewById(R.id.next_btn);
        previousBtn = findViewById(R.id.previous_btn);
        imageView = findViewById(R.id.logo);
        lowerRelativeLayout = findViewById(R.id.lower);
        voiceEnableBtn = findViewById(R.id.voice_enabled_btn);
        songNameTxt = findViewById(R.id.songName);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        validateReceivedValuesAndStartPlaying();

        // display logo of song
        imageView.setBackgroundResource(R.drawable.logo);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                // retrieve string result of user speech
                ArrayList<String> matchesFound = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matchesFound != null) {
                    if (mode.equals("ON")) { // voice mode is on so obey so act on it
                        // command record from user, so just get first command
                        keeper = matchesFound.get(0);

                        if (keeper.equalsIgnoreCase("pause the song")) { // if voice cmd is pause song
                            playPauseSong();
                            Toast.makeText(SmartPlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();
                        } else if (keeper.equalsIgnoreCase("play the song")) {  // if voice command is to play song
                            playPauseSong();
                            Toast.makeText(SmartPlayerActivity.this, "command = " + keeper, Toast.LENGTH_LONG).show();
                        }
                        else if (keeper.equalsIgnoreCase("play next song")) {  // if voice command is to play song
                            playNextSong();
                            Toast.makeText(SmartPlayerActivity.this, "command = " + keeper, Toast.LENGTH_LONG).show();
                        }
                        else if (keeper.equalsIgnoreCase("play previous song")) {  // if voice command is to play song
                            playPreviousSong();
                            Toast.makeText(SmartPlayerActivity.this, "command = " + keeper, Toast.LENGTH_LONG).show();
                        }
//                    Toast.makeText(SmartPlayerActivity.this, "Result = " + keeper, Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        keeper = "";
                        break;
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;
                }
                return false;
            }
        });

        voiceEnableBtn.setOnClickListener(v -> {
            if (mode.equals("ON")) {
                mode = "OFF";
                voiceEnableBtn.setText("Voice Enabled Mode - OFF");
                lowerRelativeLayout.setVisibility(View.VISIBLE);
            } else {  // voice mode off before clikcing on btn
                mode = "ON";
                voiceEnableBtn.setText("Voice Enabled Mode - ON");
                lowerRelativeLayout.setVisibility(View.GONE);
            }
        });

        pausePlayBtn.setOnClickListener(view -> {
            playPauseSong();
        });

        previousBtn.setOnClickListener(view -> {
            if(myMediaPlayer.getCurrentPosition() > 0){  // there must exist a song, to be returned to. Helps avoid app crash
                playPreviousSong();
            }
        });

        nextBtn.setOnClickListener(view -> {
            if(myMediaPlayer.getCurrentPosition() > 0){
                playNextSong();
            }
        });
    }


    /*
    retrieves song info from list item clicked on via bundle
     */
    private void validateReceivedValuesAndStartPlaying() {
        if (myMediaPlayer != null) {
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("song");
        position = bundle.getInt("position", 0);
        mSongName = mySongs.get(position).getName();
        String songName = intent.getStringExtra("name");

        songNameTxt.setText(songName);
        songNameTxt.setSelected(true);

        Uri uri = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer = MediaPlayer.create(this, uri);
        myMediaPlayer.start();
    }

    /*
    checks if audio/ record audio perm needed for voice command is granted
     */
    private void checkVoiceCommandPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(SmartPlayerActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                // perm not granted
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

    private void playPauseSong() {
        imageView.setBackgroundResource(R.drawable.four);

        if (myMediaPlayer.isPlaying()) {  // song is playing, so pause
            pausePlayBtn.setImageResource(R.drawable.play);
            myMediaPlayer.pause();
        } else {  // song is already paused
            pausePlayBtn.setImageResource(R.drawable.pause);
            myMediaPlayer.start();
            imageView.setBackgroundResource(R.drawable.five);
        }
    }

    private void playNextSong() {
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();  // release expensive resource for other use

        position = ((position + 1) % mySongs.size());  // gets pos of next song from the songs arrayList

        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(this, uri);
        mSongName = mySongs.get(position).toString();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();

        imageView.setBackgroundResource(R.drawable.three);

        if (myMediaPlayer.isPlaying()) {  // song is playing, so pause
            pausePlayBtn.setImageResource(R.drawable.pause);
        } else {  // song is already paused
            pausePlayBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.five);
        }
    }

    private void playPreviousSong(){
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        // if at first song in songs list, then go to last song on list when prev song btn clicked
        position = ((position - 1) < 0 ? (mySongs.size() - 1) : (position - 1));
        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(this, uri);

        mSongName = mySongs.get(position).toString();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();

        imageView.setBackgroundResource(R.drawable.two);

        if (myMediaPlayer.isPlaying()) {  // song is playing, so pause
            pausePlayBtn.setImageResource(R.drawable.pause);
        } else {  // song is already paused
            pausePlayBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.five);
        }
    }
}