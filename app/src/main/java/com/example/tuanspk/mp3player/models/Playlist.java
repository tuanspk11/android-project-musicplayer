package com.example.tuanspk.mp3player.models;

public class Playlist {

    private String name;
    private int[] listSongId;

    public Playlist(String name) {
        this.name = name;
        this.listSongId = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
