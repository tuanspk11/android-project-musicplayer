package com.example.tuanspk.soundlife.models;

public class Song {

    private int id;
    private String name;
    private String artist;
    private int duration;

    public Song(int id, String name, String artist, int duration) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
