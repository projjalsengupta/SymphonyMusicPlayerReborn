package music.symphony.com.materialmusicv2.objects;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class Song implements Key {

    private long id;
    private String name;
    private String artist;
    private long albumId;
    private String path;
    private String album;
    private int date;
    private int duration;
    private long artistId;
    private int trackNumber;
    private int year;

    public Song(long id, String name, String artist, long albumId, String path, String album, int date, int duration, long artistId, int trackNumber, int year) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.albumId = albumId;
        this.path = path;
        this.album = album;
        this.date = date;
        this.duration = duration;
        this.artistId = artistId;
        this.trackNumber = trackNumber % 1000;
        this.year = year;
    }

    public Song() {
        this.id = -1;
        this.path = "";
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getPath() {
        return path;
    }

    public String getAlbum() {
        return album;
    }

    public int getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }

    public long getArtistId() {
        return artistId;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public int getYear() {
        return year;
    }

    @Override
    public int hashCode() {
        return this.getPath().hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ByteBuffer.allocate(Integer.SIZE).putInt(hashCode()).array());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Song && this.getPath().equals(((Song) other).getPath());
    }

    @NonNull
    @Override
    public String toString() {
        return this.path;
    }
}
