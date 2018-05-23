package com.example.tuanspk.soundlife.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.activities.MainActivity;
import com.example.tuanspk.soundlife.adapters.SongAdapter;
import com.example.tuanspk.soundlife.models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListSongFragment extends Fragment {

    private ListView listViewMusic;
    private ArrayList<Song> songs;
    private SongAdapter listSongAdapter;

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public void setListSongAdapter(SongAdapter listSongAdapter) {
        this.listSongAdapter = listSongAdapter;
    }

    public SongAdapter getListSongAdapter() {
        return listSongAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        declare(view);
        init();
        listViewMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) getActivity()).songPicked(position);
            }
        });

        return view;
    }

    private void init() {
        songs = ((MainActivity) getActivity()).getSongList();
        listSongAdapter = new SongAdapter(getContext(), songs);
        listViewMusic.setAdapter(listSongAdapter);
    }

    private void declare(View view) {
        listViewMusic = view.findViewById(R.id.listview_songs);
    }

}