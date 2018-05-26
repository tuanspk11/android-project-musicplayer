package com.example.tuanspk.soundlife.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.tuanspk.soundlife.activities.MainActivity;
import com.example.tuanspk.soundlife.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {

    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int position;

    private Random random;
    private boolean isShuffle;
    private int repeat;

    private final IBinder musicBinder = new MusicBinder();

    private IServiceCallbacks serviceCallbacks;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void setServiceCallbacks(IServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return true;
    }

    private void init() {
        mediaPlayer = new MediaPlayer();
        position = 0;
        random = new Random();
        isShuffle = false;
        repeat = 0;
    }

    private void initMusicPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void play() {
        mediaPlayer.reset();

        Song playSong = songs.get(position);
        long currentSong = playSong.getId();
        songTitle = playSong.getTitle();

        // set Uri
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("Music Service", "Error setting data source", e);
        }

        mediaPlayer.prepareAsync();
//        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void resume() {
        mediaPlayer.start();
    }

    public void previous() {
        position--;
        if (position < 0)
            position = songs.size() - 1;

        play();
    }

    public void next() {
        switch (repeat) {
            case 0:
                if (isShuffle) {
                    int newSong = position;
                    while (newSong == position) {
                        newSong = random.nextInt(songs.size());
                    }
                    position = newSong;
                    play();
                } else {
                    position++;
                    if (position >= songs.size()) {
                        position = 0;
                        pause();
                    } else play();
                }
                break;
            case 1:
                if (isShuffle) {
                    int newSong = position;
                    while (newSong == position) {
                        newSong = random.nextInt(songs.size());
                    }
                    position = newSong;
                } else {
                    position++;
                    if (position >= songs.size())
                        position = 0;
                }
                play();
                break;
            case 2:
                play();
                break;
        }
    }

    public void setSong(int songIndex) {
        position = songIndex;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setListSong(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getDuration() {
        int i = mediaPlayer.getDuration();
        Log.e("duration", String.valueOf(i));
        return mediaPlayer.getDuration();
    }

    public boolean isShuffle() {
        return isShuffle;
    }

    public void setShuffle(boolean isShuffle) {
        this.isShuffle = isShuffle;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public void seek(int position) {
        mediaPlayer.seekTo(position);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mediaPlayer.getCurrentPosition() > 0) {
            mp.reset();
            next();

            if (serviceCallbacks != null)
                serviceCallbacks.onCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // start playback
        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentIntent(pendingIntent)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            not = builder.build();
        }

        startForeground(NOTIFY_ID, not);
    }
}
