package com.neo.aimusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private String[] itemsAll;  // to store names of audio file or songs on this device
    private ListView mSongsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSongsList = findViewById(R.id.songsList);

        appExternalStoragePermission();


    }

    public void appExternalStoragePermission(){
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        displayAudioSongsName();
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {

                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();   // continue reqesting for permission
                    }
                }).check();
    }

    /*
    get all audio files on this device
     */
    public ArrayList<File> readOnlyAudioSongs(File file){
        ArrayList<File> arrayList = new ArrayList<>();
        File[] allFiles = file.listFiles();   // create an array of file holding all audioFiles dir read from phone
        for(File individualFile: allFiles){
            if(individualFile.isDirectory() && !individualFile.isHidden()){
                arrayList.addAll(readOnlyAudioSongs(individualFile));  //
            } else{
                if(individualFile.getPath().endsWith(".mp3") || individualFile.getPath().endsWith(".aac")
                || individualFile.getPath().endsWith("wav") || individualFile.getPath().endsWith("wma")){
                    arrayList.add(individualFile);
                }
            }
        }
        return arrayList;
    }


    /*
    fun to get songs name
     */
    private void displayAudioSongsName(){
        final ArrayList<File> audioSongs = readOnlyAudioSongs(Environment.getExternalStorageDirectory());  // arrayList holding all songs
        itemsAll = new String[audioSongs.size()];

        for( int songCounter = 0; songCounter < audioSongs.size(); songCounter++){
            itemsAll[songCounter] = audioSongs.get(songCounter).getName();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemsAll);
        mSongsList.setAdapter(arrayAdapter);

        mSongsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String songName = mSongsList.getItemAtPosition(i).toString();  // gets songName from listView

                Intent intent = new Intent(MainActivity.this, SmartPlayerActivity.class);
                intent.putExtra("song", audioSongs);
                intent.putExtra("name", songName);
                intent.putExtra("position", i);
                startActivity(intent);
            }
        });
    }
}