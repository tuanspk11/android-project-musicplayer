package com.example.tuanspk.soundlife.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.adapters.SongAdapter;
import com.example.tuanspk.soundlife.fragments.ListSongFragment;
import com.example.tuanspk.soundlife.fragments.MiniPlayerFragment;
import com.example.tuanspk.soundlife.fragments.NowPlayingFragment;
import com.example.tuanspk.soundlife.models.BaiHat;
import com.example.tuanspk.soundlife.models.Song;
import com.example.tuanspk.soundlife.services.MusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private List<String> listPermissionsNeeded;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    ListSongFragment listSongFragment;
    MiniPlayerFragment miniPlayerFragment;
    NowPlayingFragment nowPlayingFragment;

    MusicService musicService;
    Intent serviceIntent;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            // get service
            musicService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        String[] lstPermissions = new String[]{                                                     // mang chua cac quyen can xin phep
                Manifest.permission.READ_EXTERNAL_STORAGE,                                          // quyen doc du lieu bo nho
                Manifest.permission.WAKE_LOCK                                                       // quyen chay nen ung dung
        };

        if (checkAndRequestPermissions(lstPermissions)) {
            fragmentTransaction.add(R.id.fragment_main, new ListSongFragment());
            fragmentTransaction.add(R.id.fragment_bottom, miniPlayerFragment);
            fragmentTransaction.commit();
        } else {
            if (!listPermissionsNeeded.isEmpty()) {                                                 // truyen vao cac quyen chua duoc
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),    // cho phep tu listPermissions
                        1);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (serviceIntent == null) {
            serviceIntent = new Intent(this, MusicService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }

    }

    public SongAdapter getSongAdapter() {
        ArrayList<Song> songList = getSongList();
        sortByTitle(songList);

        SongAdapter songAdt = new SongAdapter(this, songList);
        return songAdt;
    }

    public ArrayList<BaiHat> getSongList1() {
        ArrayList<BaiHat> songList = new ArrayList<BaiHat>();
        songList.add(new BaiHat(0, "Cho tôi xin một vé đi tuổi thơ", R.raw.cho_toi_xin_mot_ve_di_tuoi_tho, "Link Lee", 132000));
        songList.add(new BaiHat(1, "Đời dạy tôi", R.raw.doi_day_toi, "Only C", 94000));
        songList.add(new BaiHat(2, "Làm tình nguyện hết mình", R.raw.lam_tinh_nguyen_het_minh, "Ba Con Soi", 126000));
        songList.add(new BaiHat(3, "Mình yêu nhau đi", R.raw.minh_yeu_nhau_di, "Bích Phương", 109000));
        songList.add(new BaiHat(4, "Người âm phủ", R.raw.nguoi_am_phu, "I Dont Know", 56000));
        songList.add(new BaiHat(5, "Nơi này có anh", R.raw.noi_nay_co_anh, "Sơn Tùng MTP", 155000));
        songList.add(new BaiHat(6, "Yêu 5", R.raw.yeu_5, "I dont Know", 113000));

        return songList;
    }

    public ArrayList<Song> getSongList() {

        ArrayList<Song> songList = new ArrayList<Song>();

        // retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            // get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int duration = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DURATION);
            // add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                int thisDuration = musicCursor.getInt(duration);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisDuration));
            }
            while (musicCursor.moveToNext());
        }

        return songList;
    }

    public void songPicked(int position) {
        listSongFragment = (ListSongFragment) getFragmentManager().findFragmentById(R.id.fragment_main);

        miniPlayerFragment.setTxtSongTitle(listSongFragment.getSongs().get(position).getTitle());
        miniPlayerFragment.setTxtSongArtist(listSongFragment.getSongs().get(position).getArtist());
        fragmentTransaction.replace(R.id.fragment_bottom, miniPlayerFragment);
        miniPlayerFragment.show(miniPlayerFragment.getView());

        musicService.setListSong(listSongFragment.getSongs());
        musicService.setSong(position);
        musicService.play();
    }

    public void songPlaying() {
//        fragmentTransaction.replace(R.id.fragment_main, nowPlayingFragment);
        miniPlayerFragment.hide(miniPlayerFragment.getView());

        nowPlayingFragment = new NowPlayingFragment();
        fragmentTransaction.replace(R.id.fragment_main, nowPlayingFragment);

    }

    public void sortByTitle(ArrayList<Song> listSong) {
        Collections.sort(listSong, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    private void declare() {

    }

    private void init() {
        listPermissionsNeeded = new ArrayList<>();
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        miniPlayerFragment = new MiniPlayerFragment();
    }

    private boolean checkAndRequestPermissions(String[] listPer) {

        String[] permissions = listPer;

        int i = 0;
        for (String permission : permissions) {                                                     // lan luot kiem tra xem
            if (ContextCompat.checkSelfPermission(this,                                     // cac quyen trong mang String
                    permission) != PackageManager.PERMISSION_GRANTED) {                             // da duoc cho phep chua
                listPermissionsNeeded.add(permission);                                              // neu chua thi add vao 1 list
                i++;
            }
        }

        if (i > 0) return false;
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragmentTransaction.add(R.id.fragment_main, new ListSongFragment());
                fragmentTransaction.add(R.id.fragment_bottom, miniPlayerFragment);
                fragmentTransaction.commit();
            } else {
                Toast.makeText(MainActivity.this, "Permision Write File is Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void pause() {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void seekTo(int pos) {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return false;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
