package com.example.tuanspk.mp3player.utils;

import com.example.tuanspk.mp3player.models.Playlist;
import com.example.tuanspk.mp3player.models.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortFunctions {

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

    public void sortByName(ArrayList<Playlist> listPlaylist) {
        Collections.sort(listPlaylist, new Comparator<Playlist>() {
            public int compare(Playlist a, Playlist b) {
                return a.getName().compareTo(b.getName());
            }
        });
    }

}
