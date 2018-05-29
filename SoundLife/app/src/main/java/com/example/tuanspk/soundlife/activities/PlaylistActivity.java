package com.example.tuanspk.soundlife.activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.adapters.PlaylistAdapter;
import com.example.tuanspk.soundlife.adapters.SongAdapter;
import com.example.tuanspk.soundlife.callbacks.IPlaylistAdapterCallbacks;
import com.example.tuanspk.soundlife.fragments.ListSongInPlaylistFragment;
import com.example.tuanspk.soundlife.fragments.MiniPlayerInPlaylistFragment;
import com.example.tuanspk.soundlife.fragments.NowPlayingInPlaylistFragment;
import com.example.tuanspk.soundlife.fragments.PlaylistFragment;
import com.example.tuanspk.soundlife.models.Playlist;
import com.example.tuanspk.soundlife.helpers.IOInternalStorage;
import com.example.tuanspk.soundlife.models.Song;
import com.example.tuanspk.soundlife.callbacks.IServiceCallbacks;
import com.example.tuanspk.soundlife.services.MusicService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class PlaylistActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl, IServiceCallbacks, IPlaylistAdapterCallbacks {

    private FragmentManager fragmentManager;
    private FragmentTransaction playlistTransaction;
    private FragmentTransaction listSongTransaction;
    private FragmentTransaction miniPlayerTransaction;
    private FragmentTransaction nowPlayingTransaction;

    private PlaylistFragment playlistFragment;
    private ListSongInPlaylistFragment listSongFragment;
    private MiniPlayerInPlaylistFragment miniPlayerFragment;
    private NowPlayingInPlaylistFragment nowPlayingFragment;

    private MusicService musicService;
    private Intent serviceIntent;
    private boolean musicBound;

    private Handler handler;
    private Runnable runnable;

    private boolean isListSongFragmentShow;
    private boolean isPlayingFragmentShow;

    private boolean isShuffle;
    private int repeat;

    private IOInternalStorage IOData = new IOInternalStorage();
    private String fileName = "playlist.txt";
    private String filePath = "playlist";
    File internalFile;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            // get service
            musicService = binder.getService();

            listSongFragment = (ListSongInPlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_songs_in_playlist);
            musicService.setListSong(listSongFragment.getSongs());
            musicService.setServiceCallbacks(PlaylistActivity.this);

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        init();
        initFragment();
        playlistFragment.setPlaylistAdapter(getApplicationContext(), getPlaylistAdapter());

        filePath = "playlist";
        fileName = "playlist.txt";
        IOData.readInternal(this.getApplicationContext(), filePath, fileName);

        playlistFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<Song> songList;
        switch (item.getItemId()) {
            case R.id.item_add_playlist:
                showAddPlaylistDialog();
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
        }

        return super.onOptionsItemSelected(item);
    }

    public PlaylistAdapter getPlaylistAdapter() {
        ArrayList<Playlist> listPlaylist = getListPlaylist();
        sortByName(listPlaylist);

        PlaylistAdapter adapter = new PlaylistAdapter(this, listPlaylist);
        return adapter;
    }

    public ArrayList<Playlist> getListPlaylist() {
        ArrayList<Playlist> listPlaylist = new ArrayList<Playlist>();

        String[] list = IOData.readFileInInternal(IOData.readInternal(this.getApplicationContext(),
                "playlist", "playlist.txt"), "playlist", "playlist.txt");
        if (list != null)
            for (int i = 0; i < list.length; i++) {
                Playlist playlist = new Playlist(list[i]);
                listPlaylist.add(playlist);
            }

        return listPlaylist;
    }

    public void sortByName(ArrayList<Playlist> listPlaylist) {
        Collections.sort(listPlaylist, new Comparator<Playlist>() {
            public int compare(Playlist a, Playlist b) {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isPlayingFragmentShow) {
            getSupportActionBar().show();
            nowPlayingFragment.hide(nowPlayingFragment.getView());
            isPlayingFragmentShow = false;
        } else if (isListSongFragmentShow) {
            listSongFragment.hide(listSongFragment.getView());
            isListSongFragmentShow = false;
        } else super.onBackPressed();
    }

    public SongAdapter getSongAdapter(String playlistName) {
        ArrayList<Song> songList = getSongList(playlistName);
        sortByTitle(songList);

        SongAdapter adapter = new SongAdapter(this, songList);
        return adapter;
    }


    public ArrayList<Song> getSongList(String playlistName) {

        ArrayList<Song> songList = new ArrayList<Song>();

        MainActivity mainActivity = new MainActivity();
        String[] list = IOData.readFileInInternal(IOData.readInternal(this.getApplicationContext(),
                "playlist", playlistName), "playlist", playlistName);
        if (list != null)
            for (int i = 0; i < list.length; i++) {
                Song song = mainActivity.getSongAdapter().getSongById(i);
                if (song != null)
                    songList.add(song);
            }

        return songList;
    }

    public void playlistPicked(String playlistName) {
        setListSongFragment(getSongAdapter(playlistName));
        listSongFragment.show(listSongFragment.getView());

        createService();
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

        getSupportActionBar().hide();
        isPlayingFragmentShow = true;
    }

    public void setShuffleClicked() {
        nowPlayingFragment = (NowPlayingInPlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_playing);

        if (isShuffle) {
            showToastLengthShort(this, "Don't Shuffle");
            isShuffle = false;
            nowPlayingFragment.setShuffle(isShuffle);
            nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_not_shuffle);
            musicService.setShuffle(isShuffle);
        } else {
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
                showToastLengthShort(this, "Repeat All");
                repeat = 1;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_all);
                musicService.setRepeat(repeat);
                break;
            case 1:
                showToastLengthShort(this, "Repeat One");
                repeat = 2;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_one);
                musicService.setRepeat(repeat);
                break;
            case 2:
                showToastLengthShort(this, "Don't Repeat");
                repeat = 0;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_not_repeat);
                musicService.setRepeat(repeat);
                break;
        }
    }

    public void showAddPlaylistDialog() {
        final Dialog dialog = new Dialog(PlaylistActivity.this);
        dialog.setContentView(R.layout.layout_add_playlist);
        dialog.setCancelable(false);
        final EditText editTextPlaylistName = dialog.findViewById(R.id.edittext_playlist_name);
        final Button buttonOK = dialog.findViewById(R.id.button_ok);
        Button buttonCancel = dialog.findViewById(R.id.button_cancel);

        editTextPlaylistName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() != 0)
                    buttonOK.setEnabled(true);
                else buttonOK.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] listPlaylistName = IOData.readFileInInternal(IOData.readInternal(
                        getApplicationContext(), filePath, fileName), filePath, fileName);
                if (listPlaylistName == null)
                    listPlaylistName = new String[0];
                String[] list = new String[listPlaylistName.length + 1];
                boolean f = false;
                for (int i = 0; i < listPlaylistName.length; i++) {
                    list[i] = listPlaylistName[i];
                    if (list[i].equals(editTextPlaylistName.getText().toString().trim()))
                        f = true;
                }
                if (f) showToastLengthLong(PlaylistActivity.this, "Tên Playlist bị trùng");
                else {
                    list[listPlaylistName.length] = editTextPlaylistName.getText().toString().trim();
                    IOData.writeListPlaylistInInternal(IOData.readInternal(
                            getApplicationContext(), filePath, fileName), list);

                    playlistFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
                    playlistFragment.setPlaylistAdapter(getApplicationContext(), getPlaylistAdapter());
                    playlistFragment.setListViewPlaylist();

                    dialog.cancel();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("btn Cancel", "click");
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void setListSongFragment(SongAdapter adapter) {
        listSongFragment.setListSongAdapter(adapter);
//        listSongTransaction.replace(R.id.fragment_songs_in_playlist, listSongFragment);
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

        PlaylistActivity.this.runOnUiThread(runnable);
    }

    private void setDurationProgressBar() {
        miniPlayerFragment = (MiniPlayerInPlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_bottom);

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

        PlaylistActivity.this.runOnUiThread(runnable);
    }

    private void setDurationSeekBar() {
        nowPlayingFragment = (NowPlayingInPlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_playing);

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

    private void init() {

        fragmentManager = getFragmentManager();

        playlistTransaction = fragmentManager.beginTransaction();
        playlistFragment = new PlaylistFragment();

        listSongTransaction = fragmentManager.beginTransaction();
        listSongFragment = new ListSongInPlaylistFragment();

        miniPlayerTransaction = fragmentManager.beginTransaction();
        miniPlayerFragment = new MiniPlayerInPlaylistFragment();

        nowPlayingTransaction = fragmentManager.beginTransaction();
        nowPlayingFragment = new NowPlayingInPlaylistFragment();

        musicBound = false;

        isListSongFragmentShow = false;
        isPlayingFragmentShow = false;

        isShuffle = false;
        repeat = 0;
    }

    private void initFragment() {
        playlistTransaction.add(R.id.fragment_main, playlistFragment);
        playlistTransaction.commit();

        listSongTransaction.add(R.id.fragment_songs_in_playlist, listSongFragment);
        listSongTransaction.commit();

        miniPlayerTransaction.add(R.id.fragment_bottom, miniPlayerFragment);
        miniPlayerTransaction.commit();

        nowPlayingTransaction.add(R.id.fragment_playing, nowPlayingFragment);
        nowPlayingTransaction.commit();
    }

    public void showToastLengthShort(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void showToastLengthLong(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    public void play(int position) {
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

    @Override
    public void eventItemClick(ArrayList<Playlist> playlists) {
        String[] list = new String[playlists.size()];
        for (int i = 0; i < list.length; i++)
            list[i] = playlists.get(i).getName();
        IOData.writeListPlaylistInInternal(IOData.readInternal(
                getApplicationContext(), filePath, fileName), list);

        playlistFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
        playlistFragment.setPlaylistAdapter(getApplicationContext(), getPlaylistAdapter());
        playlistFragment.setListViewPlaylist();
    }
}
