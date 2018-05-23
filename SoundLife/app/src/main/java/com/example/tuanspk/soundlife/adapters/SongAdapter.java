package com.example.tuanspk.soundlife.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.fragments.ListSongFragment;
import com.example.tuanspk.soundlife.models.Song;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {

    //    Context context;
//    int layout;
    private ArrayList<Song> songs;
    private LayoutInflater songInflater;

    public SongAdapter(Context context, ArrayList<Song> songs) {
//        this.context = context;
//        this.layout = layout;
        this.songs = songs;
        this.songInflater = LayoutInflater.from(context);
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
    public View getView(int position, View convertView, ViewGroup parent) {

        // map to song layout
        RelativeLayout songLayout = (RelativeLayout) songInflater.inflate(
                R.layout.item_song, parent, false);

        // get title and artist views
        TextView txtTitle = (TextView) songLayout.findViewById(R.id.textview_song_title);
        TextView txtArtist = (TextView) songLayout.findViewById(R.id.textview_song_artist);
        TextView txtDuration = (TextView) songLayout.findViewById(R.id.textview_song_duration);

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

    public ArrayList<Song> getSongs() {
        return songs;
    }
}
