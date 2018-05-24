package com.example.tuanspk.soundlife.models;

public class BaiHat {

    private int id;
    private String title;
    private int resource;
    private String artist;
    private int duration;

    public BaiHat(int id, String title, int resource, String artist, int duration) {
        this.id = id;
        this.title = title;
        this.resource = resource;
        this.artist = artist;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
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
