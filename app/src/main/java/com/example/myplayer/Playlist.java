package com.example.myplayer;

import android.os.Parcel;
import android.os.Parcelable;

public class Playlist implements Parcelable {

    private String name;
    private String artist;
    private int id;

    protected Playlist(Parcel in) {
        name = in.readString();
        artist = in.readString();
        id = in.readInt();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public int getId() {
        return id;
    }

    public Playlist(String name, String artist, int id) {
        this.id = id;
        this.name = name;
        this.artist = artist;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeInt(id);
    }
}
