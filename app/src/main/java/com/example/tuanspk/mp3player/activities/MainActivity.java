package com.example.tuanspk.mp3player.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.adapters.SongAdapter;
import com.example.tuanspk.mp3player.callbacks.IServiceCallbacks;
import com.example.tuanspk.mp3player.fragments.ListSongFragment;
import com.example.tuanspk.mp3player.fragments.MiniPlayerFragment;
import com.example.tuanspk.mp3player.fragments.NowPlayingFragment;
import com.example.tuanspk.mp3player.models.Song;
import com.example.tuanspk.mp3player.services.MusicService;
import com.example.tuanspk.mp3player.utils.ShowToasFunctions;
import com.example.tuanspk.mp3player.utils.SortFunctions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl,
        IServiceCallbacks {

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

    private boolean isShowHome;
    private boolean isShowMain;

    private SimpleDateFormat simpleDateFormat;

    private SortFunctions sortFunctions;
    private ShowToasFunctions showToasFunctions;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        String[] lstPermissions = new String[]{                                                     // mang chua cac quyen can xin phep
                Manifest.permission.READ_EXTERNAL_STORAGE,                                          // quyen doc du lieu bo nho
                Manifest.permission.WAKE_LOCK,                                                      // quyen chay nen ung dung
                Manifest.permission.RECORD_AUDIO,                                                   // quyen thu am file audio
                Manifest.permission.MODIFY_AUDIO_SETTINGS                                           // quyen chinh sua file audio
        };

        if (checkAndRequestPermissions(lstPermissions)) {

            initFragment();
            listSongFragment.setListSongAdapter(getSongAdapter(getSongsInExternalStorage()));
            createService();

        } else {
            if (!listPermissionsNeeded.isEmpty()) {                                                 // truyen vao cac quyen chua duoc
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),    // cho phep tu listPermissions
                        1);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        nowPlayingFragment = (NowPlayingFragment) getFragmentManager().findFragmentById(R.id.fragment_playing);

        isShowHome = true;
        isShowMain = false;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        isShowHome = false;
        isShowMain = true;

        listSongFragment = (ListSongFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
        listSongFragment.show(listSongFragment.getView());
        musicService.setListSong(listSongFragment.getSongs());

        setMiniFragment(musicService.getPosition());
        setPlayingFragment(musicService.getPosition());
//        miniPlayerFragment.getProgressBarSong().setMax(musicService.getDuration());
//        nowPlayingFragment.getSeekBar().setMax(musicService.getDuration());

        musicService.setServiceCallbacks(MainActivity.this);
        musicBound = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(serviceConnection);
    }

    @Override
    public void onBackPressed() {
        if (isPlayingFragmentShow) {
            listSongFragment.show(listSongFragment.getView());
            miniPlayerFragment.show(miniPlayerFragment.getView());
            getSupportActionBar().show();
            isPlayingFragmentShow = false;

            nowPlayingFragment.hide(nowPlayingFragment.getView());
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getSupportActionBar().setTitle("All songs");
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
                sortFunctions.sortByTitle(songList);
                listSongFragment.setListSongAdapter(new SongAdapter(this, songList));
                listSongFragment.setListViewMusic();
                return true;
            case R.id.item_sort_artist:
                songList = listSongFragment.getSongs();
                sortFunctions.sortByArtist(songList);
                listSongFragment.setListSongAdapter(new SongAdapter(this, songList));
                listSongFragment.setListViewMusic();
                return true;
            case R.id.item_shuffle:
                if (isShuffle) {
                    itemShuffle.setTitle("Don't Shuffle");
                    itemShuffle.setIcon(R.drawable.ic_item_not_shuffle);
                    showToasFunctions.showToastLengthShort(this, "Don't Shuffle");
                    isShuffle = false;
                    musicService.setShuffle(isShuffle);
                } else {
                    itemShuffle.setTitle("Shuffle");
                    itemShuffle.setIcon(R.drawable.ic_shuffle);
                    showToasFunctions.showToastLengthShort(this, "Shuffle");
                    isShuffle = true;
                    musicService.setShuffle(isShuffle);
                }
                return true;
            case R.id.item_repeat:
                switch (repeat) {
                    case 0:
                        itemRepeat.setTitle("Repeat All");
                        itemRepeat.setIcon(R.drawable.ic_repeat_all);
                        showToasFunctions.showToastLengthShort(this, "Repeat All");
                        repeat = 1;
                        musicService.setRepeat(repeat);
                        break;
                    case 1:
                        itemRepeat.setTitle("Repeat One");
                        itemRepeat.setIcon(R.drawable.ic_repeat_one);
                        showToasFunctions.showToastLengthShort(this, "Repeat One");
                        repeat = 2;
                        musicService.setRepeat(repeat);
                        break;
                    case 2:
                        itemRepeat.setTitle("Don't Repeat");
                        itemRepeat.setIcon(R.drawable.ic_item_not_repeat);
                        showToasFunctions.showToastLengthShort(this, "Don't Repeat");
                        repeat = 0;
                        musicService.setRepeat(repeat);
                        break;
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void declareItemInActionBar(Menu menu) {
        itemShuffle = menu.findItem(R.id.item_shuffle);
        itemRepeat = menu.findItem(R.id.item_repeat);
    }

    public SongAdapter getSongAdapter(ArrayList<Song> listSong) {
        ArrayList<Song> list = listSong;
        sortFunctions.sortByTitle(list);

        SongAdapter adapter = new SongAdapter(this, list);
        return adapter;
    }

    public ArrayList<Song> getSongsInExternalStorage() {

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
            int albumIdColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM_ID);
            // add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                int thisDuration = musicCursor.getInt(duration);
                long thisAlbumId = musicCursor.getLong(albumIdColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisDuration, thisAlbumId));
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

        setPlayingFragment(musicService.getPosition());
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
//        nowPlayingFragment.loadLyrics();
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

    public void setMiniFragment(int position) {
        miniPlayerFragment.setTxtSongTitle(musicService.getSongs().get(position).getTitle());
        miniPlayerFragment.setTxtSongArtist(musicService.getSongs().get(position).getArtist());
        miniPlayerFragment.setPause(false);
//        miniPlayerTransaction.replace(R.id.fragment_bottom, miniPlayerFragment);
    }

    public void setPlayingFragment(int position) {
        nowPlayingFragment.setTxtSongTitle(musicService.getSongs().get(position).getTitle());
        nowPlayingFragment.setTxtSongArtist(musicService.getSongs().get(position).getArtist());
        nowPlayingFragment.setTxtSongDuration(simpleDateFormat.format(new Date((musicService.getDuration()))));
        nowPlayingFragment.setCircleBarVisualizer(musicService.getMediaPlayer().getAudioSessionId());
//        nowPlayingTransaction.replace(R.id.fragment_playing, nowPlayingFragment);
    }

    public void setShuffleClicked() {
        nowPlayingFragment = (NowPlayingFragment) getFragmentManager().findFragmentById(R.id.fragment_playing);

        if (isShuffle) {
            itemShuffle.setTitle("Don't Shuffle");
            itemShuffle.setIcon(R.drawable.ic_item_not_shuffle);
            showToasFunctions.showToastLengthShort(this, "Don't Shuffle");
            isShuffle = false;
            nowPlayingFragment.setShuffle(isShuffle);
            nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_not_shuffle);
            musicService.setShuffle(isShuffle);
        } else {
            itemShuffle.setTitle("Shuffle");
            itemShuffle.setIcon(R.drawable.ic_shuffle);
            showToasFunctions.showToastLengthShort(this, "Shuffle");
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
                itemRepeat.setIcon(R.drawable.ic_repeat_all);
                showToasFunctions.showToastLengthShort(this, "Repeat All");
                repeat = 1;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_all);
                musicService.setRepeat(repeat);
                break;
            case 1:
                itemRepeat.setTitle("Repeat One");
                itemRepeat.setIcon(R.drawable.ic_repeat_one);
                showToasFunctions.showToastLengthShort(this, "Repeat One");
                repeat = 2;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_one);
                musicService.setRepeat(repeat);
                break;
            case 2:
                itemRepeat.setTitle("Don't Repeat");
                itemRepeat.setIcon(R.drawable.ic_item_not_repeat);
                showToasFunctions.showToastLengthShort(this, "Don't Repeat");
                repeat = 0;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_not_repeat);
                musicService.setRepeat(repeat);
                break;
        }
    }

    private void setProgressBar(int currentProgress) {
        miniPlayerFragment.getProgressBarSong().setProgress(currentProgress);
    }

    private void setRunnableProgressBar() {
        setDurationProgressBar();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isShowMain)
                    if (musicService.isPlaying())
                        setProgressBar(musicService.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        };

        MainActivity.this.runOnUiThread(runnable);
    }

    private void setDurationProgressBar() {
        miniPlayerFragment.getProgressBarSong().setMax(musicService.getDuration());
//        int i = miniPlayerFragment.getProgressBarSong().getMax();
//        Log.e("progressbar max", String.valueOf(i));
        setProgressBar(0);
    }

    private void setRunnableSeekBar() {
        setDurationSeekBar();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isShowMain)
                    if (musicService.isPlaying()) {
//                        int i = musicService.getCurrentPosition();
//                        Log.e("current position", String.valueOf(i));
//                        Log.e("max progressbar", String.valueOf(nowPlayingFragment.getSeekBar().getMax()));
//                        Log.e("duration", String.valueOf(musicService.getDuration()));
//                        Log.e("position", String.valueOf(musicService.getPosition()));
//                        Log.e("song id", String.valueOf(musicService.getSongs().get(musicService.getPosition()).getId()));
                        setSeekBar(musicService.getCurrentPosition());
                    }
                handler.postDelayed(this, 1000);
            }
        };

        MainActivity.this.runOnUiThread(runnable);
    }

    private void setDurationSeekBar() {
        nowPlayingFragment.getSeekBar().setMax(musicService.getDuration());
//        int i = nowPlayingFragment.getSeekBar().getMax();
//        Log.e("seekbar max", String.valueOf(i));
        setSeekBar(0);
    }

    private void setSeekBar(int currentProgress) {
        nowPlayingFragment.getSeekBar().setProgress(currentProgress);
        nowPlayingFragment.setTxtSongElapedTime(simpleDateFormat.format(new Date(currentProgress)));
    }

    public void setOnSeekBarChange(int progress) {
        musicService.seek(progress);
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

        isShowHome = false;
        isShowMain = true;

        simpleDateFormat = new SimpleDateFormat("mm:ss");

        sortFunctions = new SortFunctions();
        showToasFunctions = new ShowToasFunctions();
    }

    private void initFragment() {
        listSongTransaction.add(R.id.fragment_main, listSongFragment);
        listSongTransaction.commit();

        miniPlayerTransaction.add(R.id.fragment_bottom, miniPlayerFragment);
        miniPlayerTransaction.commit();

        nowPlayingTransaction.add(R.id.fragment_playing, nowPlayingFragment);
        nowPlayingTransaction.commit();
    }

    public void play(int position) {
//        musicService.setListSong(listSongFragment.getSongs());
        musicService.setPosition(position);
        musicService.play();

        setRunnableProgressBar();
        setRunnableSeekBar();
    }

    public void next() {
        musicService.next();

        setDurationProgressBar();
        setMiniFragment(musicService.getPosition());

        setDurationSeekBar();
        setPlayingFragment(musicService.getPosition());
    }

    public void previous() {
        musicService.previous();

        setDurationProgressBar();
        setMiniFragment(musicService.getPosition());

        setDurationSeekBar();
        setPlayingFragment(musicService.getPosition());
    }

    public void stop() {
        if (musicService.isPlaying()) {
//            handler.removeCallbacks(runnable);
            musicService.pause();

            isShowMain = false;
        }
    }

    @Override
    public void onCompletion() {
        if (!isShowHome)
            if (isShowMain) {
                setDurationProgressBar();
                setMiniFragment(musicService.getPosition());

                setDurationSeekBar();
                setPlayingFragment(musicService.getPosition());
            }
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
            boolean isDenied = false;
            for (int i = 0; i < grantResults.length; i++)
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    isDenied = true;
            if (!isDenied) {
                init();
                initFragment();
                listSongFragment.setListSongAdapter(getSongAdapter(getSongsInExternalStorage()));
                createService();

            } else {
                Toast.makeText(MainActivity.this, "Permision is Denied", Toast.LENGTH_SHORT).show();
            }
        } else

        {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
