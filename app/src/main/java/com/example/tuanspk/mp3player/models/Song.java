package com.example.tuanspk.mp3player.models;

public class Song {

    private long id;
    private String title;
    private String artist;
    private int duration;
    private long albumId;

    public Song(long id, String name, String artist, int duration, long albumId) {
        this.id = id;
        this.title = name;
        this.artist = artist;
        this.duration = duration;
        this.albumId = albumId;
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

    public int getDuration() {
        return duration;
    }

}
