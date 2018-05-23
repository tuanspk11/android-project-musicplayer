package com.example.tuanspk.soundlife.models;

public class Song {

    private long id;
    private String title;
    private String artist;
    private int duration;

    public Song(long id, String name, String artist, int duration) {
        this.id = id;
        this.title = name;
        this.artist = artist;
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
