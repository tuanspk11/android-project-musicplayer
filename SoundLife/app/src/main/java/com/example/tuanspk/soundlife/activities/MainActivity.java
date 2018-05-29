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
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.adapters.SongAdapter;
import com.example.tuanspk.soundlife.fragments.ListSongFragment;
import com.example.tuanspk.soundlife.fragments.MiniPlayerFragment;
import com.example.tuanspk.soundlife.fragments.NowPlayingFragment;
import com.example.tuanspk.soundlife.models.BaiHat;
import com.example.tuanspk.soundlife.models.Song;
import com.example.tuanspk.soundlife.callbacks.IServiceCallbacks;
import com.example.tuanspk.soundlife.services.MusicService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl, IServiceCallbacks {

    private List<String> listPermissionsNeeded;

    private FragmentManager fragmentManager;
    private FragmentTransaction listSongTransaction;
    private FragmentTransaction miniPlayerTransaction;
    private FragmentTransaction nowPlayingTransaction;

    private ListSongFragment listSongFragment;
    private MiniPlayerFragment miniPlayerFragment;
    private NowPlayingFragment nowPlayingFragment;

    private MusicService musicService;
    private Intent serviceIntent;
    private boolean musicBound;

    private Handler handler;
    private Runnable runnable;

    private boolean isPlayingFragmentShow;

    private boolean isShuffle;
    private int repeat;

    private MenuItem itemShuffle;
    private MenuItem itemRepeat;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            // get service
            musicService = binder.getService();

            listSongFragment = (ListSongFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
            listSongFragment.show(listSongFragment.getView());

            musicService.setListSong(listSongFragment.getSongs());
            musicService.setServiceCallbacks(MainActivity.this);

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public MusicService getMusicService() {
        return musicService;
    }

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

            initFragment();
            listSongFragment.setListSongAdapter(getSongAdapter());

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

        String[] lstPermissions = new String[]{                                                     // mang chua cac quyen can xin phep
                Manifest.permission.READ_EXTERNAL_STORAGE,                                          // quyen doc du lieu bo nho
                Manifest.permission.WAKE_LOCK                                                       // quyen chay nen ung dung
        };

        if (checkAndRequestPermissions(lstPermissions)) {

            createService();

        } else {
            if (!listPermissionsNeeded.isEmpty()) {                                                 // truyen vao cac quyen chua duoc
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),    // cho phep tu listPermissions
                        1);
            }
        }

        listSongFragment.show(listSongFragment.getView());
    }

    @Override
    public void onBackPressed() {
        if (isPlayingFragmentShow) {
            nowPlayingFragment.hide(nowPlayingFragment.getView());

            listSongFragment.show(listSongFragment.getView());
            miniPlayerFragment.show(miniPlayerFragment.getView());
            getSupportActionBar().show();
            isPlayingFragmentShow = false;
        } else
            super.onBackPressed();
    }

    public SongAdapter getSongAdapter() {
        ArrayList<Song> songList = getSongList();
        sortByTitle(songList);

        SongAdapter adapter = new SongAdapter(this, songList);
        return adapter;
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

        play(position);

        setMiniFragment(position);
        miniPlayerFragment.show(miniPlayerFragment.getView());
    }

    public void songPlaying() {

        setPlayingFragment();
        nowPlayingFragment.setPause(!musicService.isPlaying());
        nowPlayingFragment.setShuffle(isShuffle);
        if (isShuffle)
            nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_shuffle);
        else nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_not_shuffle);

        switch (repeat) {
            case 0:
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_not_repeat);
                break;
            case 1:
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_all);
                break;
            case 2:
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_one);
                break;
        }
        nowPlayingFragment.setRepeat(repeat);
        nowPlayingFragment.show(nowPlayingFragment.getView());

        listSongFragment.hide(listSongFragment.getView());
        miniPlayerFragment.hide(miniPlayerFragment.getView());
        getSupportActionBar().hide();
        isPlayingFragmentShow = true;

//        Bundle bundle = new Bundle();
//        bundle.putString("title", miniPlayerFragment.getTxtSongTitle());
//        bundle.putString("artist", miniPlayerFragment.getTxtSongArtist());
//        bundle.putString("duration", simpleDateFormat.format(new Date(musicService.getDuration())));
//        nowPlayingFragment.setArguments(bundle);
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        transaction.add(R.id.fragment_playing, nowPlayingFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();

//        nowPlayingFragment = (NowPlayingFragment) getFragmentManager().findFragmentById(R.id.fragment_bottom);
//        nowPlayingFragment.setTxtSongTitle(miniPlayerFragment.getTxtSongTitle());
//        nowPlayingFragment.setTxtSongArtist(miniPlayerFragment.getTxtSongArtist());
//        transaction.replace(R.id.fragment_playing, nowPlayingFragment);
    }

    public void setShuffleClicked() {
        nowPlayingFragment = (NowPlayingFragment) getFragmentManager().findFragmentById(R.id.fragment_playing);

        if (isShuffle) {
            itemShuffle.setTitle("Don't Shuffle");
            showToastLengthShort(this, "Don't Shuffle");
            isShuffle = false;
            nowPlayingFragment.setShuffle(isShuffle);
            nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_not_shuffle);
            musicService.setShuffle(isShuffle);
        } else {
            itemShuffle.setTitle("Shuffle");
            showToastLengthShort(this, "Shuffle");
            isShuffle = true;
            nowPlayingFragment.setShuffle(isShuffle);
            nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_shuffle);
            musicService.setShuffle(isShuffle);
        }
    }

    public void setRepeatClicked() {
        switch (repeat) {
            case 0:
                itemRepeat.setTitle("Repeat All");
                showToastLengthShort(this, "Repeat All");
                repeat = 1;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_all);
                musicService.setRepeat(repeat);
                break;
            case 1:
                itemRepeat.setTitle("Repeat One");
                showToastLengthShort(this, "Repeat One");
                repeat = 2;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_one);
                musicService.setRepeat(repeat);
                break;
            case 2:
                itemRepeat.setTitle("Don't Repeat");
                showToastLengthShort(this, "Don't Repeat");
                repeat = 0;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_not_repeat);
                musicService.setRepeat(repeat);
                break;
        }
    }

    public void setMiniFragment(int position) {
        miniPlayerFragment.setTxtSongTitle(musicService.getSongs().get(position).getTitle());
        miniPlayerFragment.setTxtSongArtist(musicService.getSongs().get(position).getArtist());
//        miniPlayerTransaction.replace(R.id.fragment_bottom, miniPlayerFragment);
    }

    public void setPlayingFragment() {
        nowPlayingFragment.setTxtSongTitle(musicService.getSongs().get(musicService.getPosition()).getTitle());
        nowPlayingFragment.setTxtSongArtist(musicService.getSongs().get(musicService.getPosition()).getArtist());
        nowPlayingFragment.setTxtSongDuration(simpleDateFormat.format(new Date((musicService.getDuration()))));
//        nowPlayingTransaction.replace(R.id.fragment_playing, nowPlayingFragment);
    }

    private void setRunableProgressBar() {
        setDurationProgressBar();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (musicService.isPlaying()) {
                    int i = musicService.getCurrentPosition();
                    Log.e("current position", String.valueOf(i));
                    Log.e("max progressbar", String.valueOf(miniPlayerFragment.getProgressBarSong().getMax()));
                    Log.e("duration", String.valueOf(musicService.getDuration()));
                    Log.e("position", String.valueOf(musicService.getPosition()));
                    setProgressBar(musicService.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };

        MainActivity.this.runOnUiThread(runnable);
    }

    private void setDurationProgressBar() {
        miniPlayerFragment = (MiniPlayerFragment) getFragmentManager().findFragmentById(R.id.fragment_bottom);

        miniPlayerFragment.getProgressBarSong().setMax(musicService.getDuration());
        int i = miniPlayerFragment.getProgressBarSong().getMax();
        Log.e("progressbar max", String.valueOf(i));
        setProgressBar(0);
    }

    private void setProgressBar(int currentProgress) {
        miniPlayerFragment.getProgressBarSong().setProgress(currentProgress);
    }

    private void setRunableSeekBar() {
        setDurationSeekBar();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (musicService.isPlaying()) {
                    int i = musicService.getCurrentPosition();
                    Log.e("current position", String.valueOf(i));
                    Log.e("max progressbar", String.valueOf(nowPlayingFragment.getSeekBar().getMax()));
                    Log.e("duration", String.valueOf(musicService.getDuration()));
                    Log.e("position", String.valueOf(musicService.getPosition()));
                    setSeekBar(musicService.getCurrentPosition());
                }
                handler.postDelayed(this, 1000);
            }
        };

        MainActivity.this.runOnUiThread(runnable);
    }

    private void setDurationSeekBar() {
        nowPlayingFragment = (NowPlayingFragment) getFragmentManager().findFragmentById(R.id.fragment_playing);

        nowPlayingFragment.getSeekBar().setMax(musicService.getDuration());
        int i = nowPlayingFragment.getSeekBar().getMax();
        Log.e("seekbar max", String.valueOf(i));
        setSeekBar(0);
    }

    private void setSeekBar(int currentProgress) {
        nowPlayingFragment.getSeekBar().setProgress(currentProgress);
        nowPlayingFragment.setTxtSongElapedTime(simpleDateFormat.format(new Date(currentProgress)));
    }

    public void setOnSeekBarChange(int progress) {
        musicService.seek(progress);
    }

    public void sortByTitle(ArrayList<Song> listSong) {
        Collections.sort(listSong, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
    }

    public void sortByArtist(ArrayList<Song> listSong) {
        Collections.sort(listSong, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getArtist().compareTo(b.getArtist());
            }
        });
    }

    private void declare() {
    }

    private void declareItemInActionBar(Menu menu) {
        itemShuffle = menu.findItem(R.id.item_shuffle);
        itemRepeat = menu.findItem(R.id.item_repeat);
    }

    private void init() {
        listPermissionsNeeded = new ArrayList<>();

        fragmentManager = getFragmentManager();

        listSongTransaction = fragmentManager.beginTransaction();
        listSongFragment = new ListSongFragment();

        miniPlayerTransaction = fragmentManager.beginTransaction();
        miniPlayerFragment = new MiniPlayerFragment();

        nowPlayingTransaction = fragmentManager.beginTransaction();
        nowPlayingFragment = new NowPlayingFragment();

        musicBound = false;

        isPlayingFragmentShow = false;

        isShuffle = false;
        repeat = 0;
    }

    private void initFragment() {
        listSongTransaction.add(R.id.fragment_main, listSongFragment);
        listSongTransaction.commit();

        miniPlayerTransaction.add(R.id.fragment_bottom, miniPlayerFragment);
        miniPlayerTransaction.commit();

        nowPlayingTransaction.add(R.id.fragment_playing, nowPlayingFragment);
        nowPlayingTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        declareItemInActionBar(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<Song> songList;
        switch (item.getItemId()) {
            case R.id.item_playlist:
                stop();
                Intent intent = new Intent(MainActivity.this, PlaylistActivity.class);
                startActivity(intent);
                listSongFragment.hide(listSongFragment.getView());
                miniPlayerFragment.hide(miniPlayerFragment.getView());
                return true;
            case R.id.item_sort_title:
                songList = listSongFragment.getSongs();
                sortByTitle(songList);
                listSongFragment.setListSongAdapter(new SongAdapter(this, songList));
                listSongFragment.setListViewMusic();
                return true;
            case R.id.item_sort_artist:
                songList = listSongFragment.getSongs();
                sortByArtist(songList);
                listSongFragment.setListSongAdapter(new SongAdapter(this, songList));
                listSongFragment.setListViewMusic();
                return true;
            case R.id.item_shuffle:
                if (isShuffle) {
                    itemShuffle.setTitle("Don't Shuffle");
                    showToastLengthShort(this, "Don't Shuffle");
                    isShuffle = false;
                    musicService.setShuffle(isShuffle);
                } else {
                    itemShuffle.setTitle("Shuffle");
                    showToastLengthShort(this, "Shuffle");
                    isShuffle = true;
                    musicService.setShuffle(isShuffle);
                }
                return true;
            case R.id.item_repeat:
                switch (repeat) {
                    case 0:
                        itemRepeat.setTitle("Repeat All");
                        showToastLengthShort(this, "Repeat All");
                        repeat = 1;
                        musicService.setRepeat(repeat);
                        break;
                    case 1:
                        itemRepeat.setTitle("Repeat One");
                        showToastLengthShort(this, "Repeat One");
                        repeat = 2;
                        musicService.setRepeat(repeat);
                        break;
                    case 2:
                        itemRepeat.setTitle("Don't Repeat");
                        showToastLengthShort(this, "Don't Repeat");
                        repeat = 0;
                        musicService.setRepeat(repeat);
                        break;
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showToastLengthShort(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void play(int position) {
//        musicService.setListSong(listSongFragment.getSongs());
        musicService.setPosition(position);
        musicService.play();

        setRunableProgressBar();
        setRunableSeekBar();
    }

    public void next() {
        musicService.next();

        setDurationProgressBar();
        setMiniFragment(musicService.getPosition());

        setDurationSeekBar();
        setPlayingFragment();
    }

    public void previous() {
        musicService.previous();

        setDurationProgressBar();
        setMiniFragment(musicService.getPosition());

        setDurationSeekBar();
        setPlayingFragment();
    }

    public void stop() {
        if (musicService.isPlaying())
            musicService.pause();
        if (handler != null)
            handler.removeCallbacks(runnable);
    }

    @Override
    public void onCompletion() {
        setDurationProgressBar();
        setMiniFragment(musicService.getPosition());

        setDurationSeekBar();
        setPlayingFragment();
    }

    @Override
    public void start() {
        musicService.resume();

        miniPlayerFragment.getBtnPlayPause().setBackgroundResource(R.drawable.ic_mini_pause);
        miniPlayerFragment.setPause(false);

        nowPlayingFragment.getBtnPlayPause().setBackgroundResource(R.drawable.ic_pause);
        nowPlayingFragment.setPause(false);
    }

    @Override
    public void pause() {
        musicService.pause();

        miniPlayerFragment.getBtnPlayPause().setBackgroundResource(R.drawable.ic_mini_play);
        miniPlayerFragment.setPause(true);

        nowPlayingFragment.getBtnPlayPause().setBackgroundResource(R.drawable.ic_play);
        nowPlayingFragment.setPause(false);
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying())
            return musicService.getDuration();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying())
            return musicService.getCurrentPosition();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound)
            return musicService.isPlaying();
        else return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void createService() {
        if (serviceIntent == null) {
            serviceIntent = new Intent(this, MusicService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
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

                initFragment();
                listSongFragment.setListSongAdapter(getSongAdapter());
                createService();

            } else {
                Toast.makeText(MainActivity.this, "Permision Write File is Denied", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
