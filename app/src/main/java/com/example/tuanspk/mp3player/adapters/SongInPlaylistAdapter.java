package com.example.tuanspk.mp3player.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.callbacks.ISongAdapterCallbacks;
import com.example.tuanspk.mp3player.models.Song;

import java.util.ArrayList;

public class SongInPlaylistAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Song> songs;
    private LayoutInflater songInflater;

    private ISongAdapterCallbacks songAdapterCallbacks;

    public void setSongAdapterCallbacks(ISongAdapterCallbacks callbacks) {
        songAdapterCallbacks = callbacks;
    }

    public SongInPlaylistAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
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

    private class ViewHolder {
        TextView title;
        TextView artist;
        TextView duration;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        // map to song layout
        RelativeLayout songLayout = (RelativeLayout) songInflater.inflate(
                R.layout.item_song, parent, false);

        // declare
        TextView txtTitle = (TextView) songLayout.findViewById(R.id.textview_song_title);
        TextView txtArtist = (TextView) songLayout.findViewById(R.id.textview_song_artist);
        TextView txtDuration = (TextView) songLayout.findViewById(R.id.textview_song_duration);
        ImageView buttonOption = songLayout.findViewById(R.id.button_song_option);
        buttonOption.setTag(getItem(position));
        buttonOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, position);
//                Log.e("song position", String.valueOf(position));
            }
        });

        // get song using postion
        Song currentSong = songs.get(position);

        // get title and artist strings
        txtTitle.setText(currentSong.getTitle());
        txtArtist.setText(currentSong.getArtist());
        int minute = currentSong.getDuration() / 60000;
        int second = currentSong.getDuration() / 1000 % 60;
        String secondString = String.valueOf(second);
        if (second < 10) secondString = "0" + secondString;
        txtDuration.setText(minute + ":" + secondString);

        // set position as tag
        songLayout.setTag(position);

        return songLayout;

//        ViewHolder viewHolder;
//        if (convertView == null) {
//            viewHolder = new ViewHolder();
//            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = layoutInflater.inflate(layout, null);
//            viewHolder.title = convertView.findViewById(R.id.textview_song_title);
//            viewHolder.artist = convertView.findViewById(R.id.textview_song_artist);
//            viewHolder.duration = convertView.findViewById(R.id.textview_song_duration);
//            convertView.setTag(viewHolder);
//        } else
//            viewHolder = (ViewHolder) convertView.getTag();
//
//        viewHolder.title.setText(songs.get(position).getTitle());
//        int minute = songs.get(position).getDuration() / 60000;
//        int second = songs.get(position).getDuration() / 1000 % 60;
//        String secondValue = String.valueOf(second);
//        if (second < 10) secondValue = "0" + secondValue;
//        viewHolder.duration.setText(minute + ":" + secondValue);
//        viewHolder.artist.setText(songs.get(position).getArtist());
//
//        return convertView;
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu menu = new PopupMenu(context, view);
        menu.getMenuInflater().inflate(R.menu.menu_item_song_in_playlist, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_add_to_playlist:
                        if (songAdapterCallbacks != null)
                            songAdapterCallbacks.addSongToPlaylist();
                        return true;
                    case R.id.item_remove:
                        if (songAdapterCallbacks != null)
                            songAdapterCallbacks.removeSongInPlaylist(
                                    String.valueOf(songs.get(position).getId()));
                        return true;
                }
                return true;
            }
        });
        menu.show();
    }

}
