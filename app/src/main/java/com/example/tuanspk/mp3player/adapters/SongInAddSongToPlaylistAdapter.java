package com.example.tuanspk.mp3player.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.callbacks.ISongInAddSongToPlaylistAdapterCallbacks;
import com.example.tuanspk.mp3player.models.Song;

import java.util.ArrayList;

public class SongInAddSongToPlaylistAdapter extends BaseAdapter {

    private ArrayList<Song> songs;
    private LayoutInflater songInflater;

    private int buttonOKEnable = 0;

    private ISongInAddSongToPlaylistAdapterCallbacks songInAddSongToPlaylistAdapterCallbacks;

    public void setSongInAddSongToPlaylistAdapterCallbacks(ISongInAddSongToPlaylistAdapterCallbacks callbacks) {
        songInAddSongToPlaylistAdapterCallbacks = callbacks;
    }

    public SongInAddSongToPlaylistAdapter(Context context, ArrayList<Song> songs) {
        this.songs = songs;
        this.songInflater = LayoutInflater.from(context);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // map to song layout
        RelativeLayout songLayout = (RelativeLayout) songInflater.inflate(
                R.layout.item_song_in_list_all_songs, parent, false);

        // declare
        TextView txtTitle = (TextView) songLayout.findViewById(R.id.textview_song_title);
        final CheckBox checkBoxAddSong = (CheckBox) songLayout.findViewById(R.id.checkbox_add_song);
        checkBoxAddSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxAddSong.isChecked())
                    buttonOKEnable++;
                else buttonOKEnable--;
                if (songInAddSongToPlaylistAdapterCallbacks != null)
                    songInAddSongToPlaylistAdapterCallbacks.checkBox(
                            String.valueOf(songs.get(position).getId()).trim(),
                            checkBoxAddSong.isChecked(), buttonOKEnable);
            }
        });

        // get song using postion
        Song currentSong = songs.get(position);

        // get title string
        txtTitle.setText(currentSong.getTitle());

        // set position as tag
        songLayout.setTag(position);

        return songLayout;
    }

}
