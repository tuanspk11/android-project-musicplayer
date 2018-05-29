package com.example.tuanspk.soundlife.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.tuanspk.soundlife.R;
import com.example.tuanspk.soundlife.activities.MainActivity;
import com.example.tuanspk.soundlife.activities.PlaylistActivity;
import com.example.tuanspk.soundlife.adapters.PlaylistAdapter;
import com.example.tuanspk.soundlife.callbacks.IPlaylistAdapterCallbacks;
import com.example.tuanspk.soundlife.models.Playlist;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {

    private ListView listViewPlaylist;
    private ArrayList<Playlist> listPlaylist;
    private PlaylistAdapter playlistAdapter;

    public ArrayList<Playlist> getListPlaylist() {
        return listPlaylist;
    }

    public void setSongs(ArrayList<Playlist> listPlaylist) {
        this.listPlaylist = listPlaylist;
    }

    public void setPlaylistAdapter(Context context, PlaylistAdapter playlistAdapter) {
        this.playlistAdapter = playlistAdapter;
    }

    public PlaylistAdapter getPlaylistAdapter() {
        return playlistAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        declare(view);

        setListViewPlaylist();
        listPlaylist = playlistAdapter.getListPlaylist();
        listViewPlaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("item click", "position: " + position);
                ((PlaylistActivity) getActivity()).playlistPicked(listPlaylist.get(position).getName());
            }
        });

//        buttonPlaylistOption.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("button playlist option", "click");
//            }
//        });

        return view;
    }

    public void setListViewPlaylist() {
        listViewPlaylist.setAdapter(playlistAdapter);
        playlistAdapter.setPlaylistAdapterCallbacks((PlaylistActivity) getActivity());
    }

    private void init() {

    }

    private void declare(View view) {
        listViewPlaylist = view.findViewById(R.id.listview_list_playlist);
    }

    public void show(View view) {
        view.setVisibility(view.VISIBLE);
    }

    public void hide(View view) {
        view.setVisibility(view.GONE);
    }

}
