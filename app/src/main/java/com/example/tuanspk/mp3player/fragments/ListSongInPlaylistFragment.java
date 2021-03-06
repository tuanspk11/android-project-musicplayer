package com.example.tuanspk.mp3player.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.tuanspk.mp3player.R;
import com.example.tuanspk.mp3player.activities.PlaylistActivity;
import com.example.tuanspk.mp3player.adapters.SongInPlaylistAdapter;
import com.example.tuanspk.mp3player.models.Song;

import java.util.ArrayList;

public class ListSongInPlaylistFragment extends Fragment {

    private ListView listViewMusic;
    private ArrayList<Song> songs;
    private SongInPlaylistAdapter listSongAdapter;

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setListSongAdapter(SongInPlaylistAdapter listSongAdapter) {
        this.listSongAdapter = listSongAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        declare(view);

        setListViewMusic();

        return view;
    }

    public void setListViewMusic() {
        if (listSongAdapter != null) {
            songs = listSongAdapter.getSongs();
            listViewMusic.setAdapter(listSongAdapter);
            listViewMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Log.e("item click", "position: " + position);
                    ((PlaylistActivity) getActivity()).songPicked(position);
                }
            });

            listSongAdapter.setSongAdapterCallbacks((PlaylistActivity) getActivity());
        }
    }

    private void declare(View view) {
        listViewMusic = view.findViewById(R.id.listview_songs);
    }

    public void show(View view) {
        view.setVisibility(view.VISIBLE);
    }

    public void hide(View view) {
        view.setVisibility(view.GONE);
    }

}