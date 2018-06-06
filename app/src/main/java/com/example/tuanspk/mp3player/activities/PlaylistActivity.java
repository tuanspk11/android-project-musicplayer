package com.example.tuanspk.mp3player.activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.adapters.PlaylistAdapter;
import com.example.tuanspk.mp3player.adapters.SongInPlaylistAdapter;
import com.example.tuanspk.mp3player.adapters.SongInAddSongToPlaylistAdapter;
import com.example.tuanspk.mp3player.callbacks.IPlaylistAdapterCallbacks;
import com.example.tuanspk.mp3player.callbacks.IServiceCallbacks;
import com.example.tuanspk.mp3player.callbacks.ISongAdapterCallbacks;
import com.example.tuanspk.mp3player.callbacks.ISongInAddSongToPlaylistAdapterCallbacks;
import com.example.tuanspk.mp3player.fragments.ListSongInPlaylistFragment;
import com.example.tuanspk.mp3player.fragments.MiniPlayerInPlaylistFragment;
import com.example.tuanspk.mp3player.fragments.NowPlayingInPlaylistFragment;
import com.example.tuanspk.mp3player.fragments.PlaylistFragment;
import com.example.tuanspk.mp3player.utils.IOInternalStorage;
import com.example.tuanspk.mp3player.models.Playlist;
import com.example.tuanspk.mp3player.models.Song;
import com.example.tuanspk.mp3player.services.MusicService;
import com.example.tuanspk.mp3player.utils.ShowToasFunctions;
import com.example.tuanspk.mp3player.utils.SortFunctions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PlaylistActivity extends AppCompatActivity
        implements MediaController.MediaPlayerControl, IServiceCallbacks, IPlaylistAdapterCallbacks,
        ISongInAddSongToPlaylistAdapterCallbacks,
        ISongAdapterCallbacks {

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

    private IOInternalStorage IOData;
    private String fileName;
    private String filePath;

    private MenuItem itemAddPlaylistOrSongs;

    private Button buttonOKInAddSongsToPlaylist;

    private String plName;
    private String[] listSId;
    private int listSIdLength;

    private boolean isShowPlaylist;

    private SimpleDateFormat simpleDateFormat;

    private SortFunctions sortFunctions;
    private ShowToasFunctions showToasFunctions;

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
        playlistFragment.setPlaylistAdapter(getApplicationContext(), getPlaylistAdapter(
                getListPlaylist(getFileInInternalStorage(filePath, fileName))));

        IOData.readInternal(this.getApplicationContext(), filePath, fileName);

        playlistFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        createService();
    }

    @Override
    public void onBackPressed() {
        if (isPlayingFragmentShow) {
            getSupportActionBar().show();
            nowPlayingFragment.hide(nowPlayingFragment.getView());
            isPlayingFragmentShow = false;
        } else if (isListSongFragmentShow) {
            playlistFragment.show(playlistFragment.getView());
            listSongFragment.hide(listSongFragment.getView());
            getSupportActionBar().setTitle("Playlist");
            itemAddPlaylistOrSongs.setIcon(R.drawable.ic_item_playlist_add);
            isListSongFragmentShow = false;
        } else {
            super.onBackPressed();

            stop();
            unbindService(serviceConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
        itemAddPlaylistOrSongs = menu.findItem(R.id.item_add_playlist);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<Song> songList;
        switch (item.getItemId()) {
            case R.id.item_add_playlist:
                if (isListSongFragmentShow)
                    showAddSongsDialog();
                else
                    showAddPlaylistDialog();
                return true;
            case R.id.item_sort_title:
                songList = listSongFragment.getSongs();
                sortFunctions.sortByTitle(songList);
                listSongFragment.setListSongAdapter(new SongInPlaylistAdapter(this, songList));
                listSongFragment.setListViewMusic();
                return true;
            case R.id.item_sort_artist:
                songList = listSongFragment.getSongs();
                sortFunctions.sortByArtist(songList);
                listSongFragment.setListSongAdapter(new SongInPlaylistAdapter(this, songList));
                listSongFragment.setListViewMusic();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PlaylistAdapter getPlaylistAdapter(ArrayList<Playlist> listPlaylist) {
        ArrayList<Playlist> list = listPlaylist;
//        sortByName(list);

        PlaylistAdapter adapter = new PlaylistAdapter(this, list);
        return adapter;
    }

    public SongInPlaylistAdapter getSongInPlaylistAdapter(ArrayList<Song> listSong) {
        ArrayList<Song> list = listSong;
        sortFunctions.sortByTitle(list);

        SongInPlaylistAdapter adapter = new SongInPlaylistAdapter(this, list);
        return adapter;
    }

    public ArrayList<Playlist> getListPlaylist(String[] fileInInternalStorage) {
        ArrayList<Playlist> listPlaylist = new ArrayList<Playlist>();

        String[] list = fileInInternalStorage;
        if (list != null)
            for (int i = 0; i < list.length; i++) {
                Playlist playlist = new Playlist(list[i]);
                listPlaylist.add(playlist);
            }

        return listPlaylist;
    }

    public ArrayList<Song> getSongsInPlaylist(String playlistName) {
        ArrayList<Song> list = new ArrayList<Song>();

        String[] listSongId = getFileInInternalStorage(filePath, playlistName);
        if (listSongId != null) {
            ArrayList<Song> listSongs = getSongsInExternalStorage();
            for (int i = 0; i < listSongId.length; i++)
                for (int j = 0; j < listSongs.size(); j++)
                    if (listSongId[i].toString().trim().equals(String.valueOf(listSongs.get(j).getId())))
                        list.add(listSongs.get(j));
        }

        return list;
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

    public String[] getFileInInternalStorage(String filePath, String fileName) {
        String[] list = IOData.readFileInInternal(
                IOData.readInternal(getApplicationContext(), filePath, fileName));
        if (list != null)
            return list;

        return null;
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
                        getApplicationContext(), filePath, fileName));
                if (listPlaylistName == null)
                    listPlaylistName = new String[0];
                String[] list = new String[listPlaylistName.length + 1];
                boolean f = false;
                for (int i = 0; i < listPlaylistName.length; i++) {
                    list[i] = listPlaylistName[i];
                    if (list[i].equals(editTextPlaylistName.getText().toString().trim()) ||
                            editTextPlaylistName.getText().toString().trim().equals("playlist"))
                        f = true;
                }
                if (f)
                    showToasFunctions.showToastLengthShort(PlaylistActivity.this, "Tên Playlist bị trùng");
                else {
                    list[listPlaylistName.length] = editTextPlaylistName.getText().toString().trim();
                    IOData.writeFileInInternal(IOData.readInternal(
                            getApplicationContext(), filePath, fileName), list);
                    IOData.readInternal(getApplicationContext(),
                            filePath, editTextPlaylistName.getText().toString().trim()).delete();

                    playlistFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
                    playlistFragment.setPlaylistAdapter(getApplicationContext(), getPlaylistAdapter(
                            getListPlaylist(getFileInInternalStorage(filePath, fileName))));
                    playlistFragment.setListViewPlaylist();

                    dialog.cancel();
                }
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("btn Cancel", "click");
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void showAddSongsDialog() {
        final Dialog dialog = new Dialog(PlaylistActivity.this);
        dialog.setContentView(R.layout.layout_add_songs_to_playlist);
        dialog.setCancelable(false);

        final ListView listView = dialog.findViewById(R.id.listview_list_all_songs);
        ArrayList<Song> l1 = getSongsInPlaylist(plName);
        ArrayList<Song> l2 = getSongsInExternalStorage();
        for (int i = 0; i < l1.size(); i++)
            for (int j = 0; j < l2.size(); j++)
                if (String.valueOf(l2.get(j).getId()).equals(String.valueOf(l1.get(i).getId())))
                    l2.remove(l2.get(j));
        SongInAddSongToPlaylistAdapter adapter = new SongInAddSongToPlaylistAdapter(
                PlaylistActivity.this, l2);
        listView.setAdapter(adapter);
        adapter.setSongInAddSongToPlaylistAdapterCallbacks(PlaylistActivity.this);

        buttonOKInAddSongsToPlaylist = dialog.findViewById(R.id.button_ok);
        buttonOKInAddSongsToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = IOData.readInternal(getApplicationContext(),
                        filePath, plName);
                String[] listSongID = IOData.readFileInInternal(file);
                String[] l = new String[listSIdLength];
                for (int i = 0; i < listSIdLength; i++)
                    l[i] = listSId[i];
                if (listSongID != null) {
                    String[] list = new String[listSongID.length + listSIdLength];
                    for (int i = 0; i < listSongID.length + listSIdLength; i++)
                        if (i < listSongID.length)
                            list[i] = listSongID[i];
                        else list[i] = listSId[i - listSongID.length];
                    IOData.writeFileInInternal(file, list);
                } else IOData.writeFileInInternal(file, l);

                listSongFragment = (ListSongInPlaylistFragment)
                        getFragmentManager().findFragmentById(R.id.fragment_songs_in_playlist);
                setListSongFragment(getSongInPlaylistAdapter(getSongsInPlaylist(plName)));
                musicService.setListSong(listSongFragment.getSongs());

                listSIdLength = 0;
                dialog.cancel();
            }
        });

        Button buttonCancel = dialog.findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.e("btn Cancel", "click");

                listSIdLength = 0;
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public void playlistPicked(String playlistName) {
        setListSongFragment(getSongInPlaylistAdapter(getSongsInPlaylist(playlistName)));
        listSongFragment.show(listSongFragment.getView());
        listSongFragment = (ListSongInPlaylistFragment)
                getFragmentManager().findFragmentById(R.id.fragment_songs_in_playlist);
        if (listSongFragment.getSongs().size() != 0)
            musicService.setListSong(listSongFragment.getSongs());
        playlistFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
        playlistFragment.hide(playlistFragment.getView());

//        Log.e("songs in playlist", String.valueOf(isListSongFragmentShow));
        getSupportActionBar().setTitle(playlistName);
        itemAddPlaylistOrSongs.setIcon(R.drawable.ic_add_songs);
        isListSongFragmentShow = true;

        plName = playlistName;
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
        else
            nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_not_shuffle);

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

    public void setListSongFragment(SongInPlaylistAdapter adapter) {
        listSongFragment.setListSongAdapter(adapter);
        listSongFragment.setListViewMusic();
    }

    public void setMiniFragment(int position) {
        miniPlayerFragment.setTxtSongTitle(musicService.getSongs().get(position).getTitle());
        miniPlayerFragment.setTxtSongArtist(musicService.getSongs().get(position).getArtist());
        miniPlayerFragment.setPause(false);
    }

    public void setPlayingFragment() {
        nowPlayingFragment.setTxtSongTitle(musicService.getSongs().get(musicService.getPosition()).getTitle());
        nowPlayingFragment.setTxtSongArtist(musicService.getSongs().get(musicService.getPosition()).getArtist());
        nowPlayingFragment.setTxtSongDuration(simpleDateFormat.format(new Date((musicService.getDuration()))));
        nowPlayingFragment.setCircleBarVisualizer(musicService.getMediaPlayer().getAudioSessionId());
    }

    public void setShuffleClicked() {
        nowPlayingFragment = (NowPlayingInPlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_playing);

        if (isShuffle) {
            showToasFunctions.showToastLengthShort(this, "Don't Shuffle");
            isShuffle = false;
            nowPlayingFragment.setShuffle(isShuffle);
            nowPlayingFragment.getBtnShuffle().setBackgroundResource(R.drawable.ic_not_shuffle);
            musicService.setShuffle(isShuffle);
        } else {
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
                showToasFunctions.showToastLengthShort(this, "Repeat All");
                repeat = 1;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_all);
                musicService.setRepeat(repeat);
                break;
            case 1:
                showToasFunctions.showToastLengthShort(this, "Repeat One");
                repeat = 2;
                nowPlayingFragment.setRepeat(repeat);
                nowPlayingFragment.getBtnRepeat().setBackgroundResource(R.drawable.ic_repeat_one);
                musicService.setRepeat(repeat);
                break;
            case 2:
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

    private void setRunableProgressBar() {
        setDurationProgressBar();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isShowPlaylist)
                    if (musicService.isPlaying()) {
                        int i = musicService.getCurrentPosition();
//                        Log.e("current position", String.valueOf(i));
//                        Log.e("max progressbar", String.valueOf(miniPlayerFragment.getProgressBarSong().getMax()));
//                        Log.e("duration", String.valueOf(musicService.getDuration()));
//                        Log.e("position", String.valueOf(musicService.getPosition()));
                        setProgressBar(musicService.getCurrentPosition());
                    }
                handler.postDelayed(this, 1000);
            }
        };

        PlaylistActivity.this.runOnUiThread(runnable);
    }

    private void setDurationProgressBar() {
        miniPlayerFragment.getProgressBarSong().setMax(musicService.getDuration());
        int i = miniPlayerFragment.getProgressBarSong().getMax();
//        Log.e("progressbar max", String.valueOf(i));
        setProgressBar(0);
    }

    private void setSeekBar(int currentProgress) {
        nowPlayingFragment.getSeekBar().setProgress(currentProgress);
        nowPlayingFragment.setTxtSongElapedTime(simpleDateFormat.format(new Date(currentProgress)));
    }

    private void setRunableSeekBar() {
        setDurationSeekBar();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isShowPlaylist)
                    if (musicService.isPlaying()) {
                        int i = musicService.getCurrentPosition();
//                        Log.e("current position", String.valueOf(i));
//                        Log.e("max progressbar", String.valueOf(nowPlayingFragment.getSeekBar().getMax()));
//                        Log.e("duration", String.valueOf(musicService.getDuration()));
//                        Log.e("position", String.valueOf(musicService.getPosition()));
                        setSeekBar(musicService.getCurrentPosition());
                    }
                handler.postDelayed(this, 1000);
            }
        };

        PlaylistActivity.this.runOnUiThread(runnable);
    }

    private void setDurationSeekBar() {
        nowPlayingFragment.getSeekBar().setMax(musicService.getDuration());
        int i = nowPlayingFragment.getSeekBar().getMax();
//        Log.e("seekbar max", String.valueOf(i));
        setSeekBar(0);
    }

    public void setOnSeekBarChange(int progress) {
        musicService.seek(progress);
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

        IOData = new IOInternalStorage();
        filePath = "playlist";
        fileName = "playlist.txt";

        musicBound = false;

        isListSongFragmentShow = false;
        isPlayingFragmentShow = false;

        isShuffle = false;
        repeat = 0;

        listSId = new String[getSongsInExternalStorage().size()];
        plName = "";
        listSIdLength = 0;

        isShowPlaylist = true;

        simpleDateFormat = new SimpleDateFormat("mm:ss");

        sortFunctions = new SortFunctions();
        showToasFunctions = new ShowToasFunctions();

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

    public void stop() {
        if (musicService.isPlaying()) {
//            handler.removeCallbacks(runnable);
            musicService.pause();
            isShowPlaylist = false;
        }
    }

    @Override
    public void onCompletion() {
        if (isShowPlaylist) {
            setDurationProgressBar();
            setMiniFragment(musicService.getPosition());

            setDurationSeekBar();
            setPlayingFragment();
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

    @Override
    public void eventItemClick(ArrayList<Playlist> playlists) {
        String[] list = new String[playlists.size()];
        for (int i = 0; i < list.length; i++)
            list[i] = playlists.get(i).getName();
        IOData.writeFileInInternal(IOData.readInternal(
                getApplicationContext(), filePath, fileName), list);

        playlistFragment = (PlaylistFragment) getFragmentManager().findFragmentById(R.id.fragment_main);
        playlistFragment.setPlaylistAdapter(getApplicationContext(), getPlaylistAdapter(
                getListPlaylist(getFileInInternalStorage(filePath, fileName))));
        playlistFragment.setListViewPlaylist();
    }

    @Override
    public void checkBox(String Id, boolean isChecked, int buttonOKEnable) {
        if (buttonOKEnable > 0)
            buttonOKInAddSongsToPlaylist.setEnabled(true);
        else buttonOKInAddSongsToPlaylist.setEnabled(false);
//        showToastLengthLong(this, "checked");
//        Log.e("id", Id);
//        Log.e("isChecked", String.valueOf(isChecked));

        if (isChecked) {
            listSId[listSIdLength] = Id.toString().trim();
            listSIdLength++;
        } else {
            for (int i = 0; i < listSIdLength; i++)
                if (listSId[i].toString().trim().equals(Id.toString().trim())) {
                    for (int j = i; j < listSIdLength - 1; j++)
                        listSId[j] = listSId[j + 1];
                    listSId[listSIdLength - 1] = null;
                    listSIdLength--;
                }
        }
    }

    @Override
    public void addSongToPlaylist() {

    }

    @Override
    public void removeSongInPlaylist(String songID) {
        if (musicService.isPlaying())
            showToasFunctions.showToastLengthShort(
                    this, "Music service is playing, please pause to music!!");
        else
            deleteSong(songID);
    }

    private void deleteSong(String songID) {
        File file = IOData.readInternal(getApplicationContext(), filePath, plName);
        String[] listSongID = IOData.readFileInInternal(file);
        String[] list = new String[listSongID.length - 1];
        int i = 0;
        while (!listSongID[i].toString().trim().equals(songID.toString().trim())) {
            list[i] = listSongID[i];
            i++;
        }
        for (int j = i; j < list.length; j++)
            list[j] = listSongID[j + 1];

        IOData.writeFileInInternal(file, list);

        setListSongFragment(getSongInPlaylistAdapter(getSongsInPlaylist(plName)));
        miniPlayerFragment.hide(miniPlayerFragment.getView());
        musicService.setListSong(listSongFragment.getSongs());
    }

    private void createService() {
        if (serviceIntent == null) {
            serviceIntent = new Intent(this, MusicService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

}
