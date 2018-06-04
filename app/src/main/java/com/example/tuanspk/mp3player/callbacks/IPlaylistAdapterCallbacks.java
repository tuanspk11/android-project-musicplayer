package com.example.tuanspk.mp3player.callbacks;

import com.example.tuanspk.mp3player.models.Playlist;

import java.util.ArrayList;

public interface IPlaylistAdapterCallbacks {
    void eventItemClick(ArrayList<Playlist> playlists);
}
