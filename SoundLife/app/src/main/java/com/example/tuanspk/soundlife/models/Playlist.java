package com.example.tuanspk.soundlife.models;

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

    public int[] getListSongId() {
        return listSongId;
    }

    public void setListSongId(int[] listSongId) {
        this.listSongId = listSongId;
    }

}
