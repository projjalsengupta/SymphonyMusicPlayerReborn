package music.symphony.com.materialmusicv2.objects;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;

public class Artist implements Key {

    private static final String UNKNOWN_ARTIST_DISPLAY_NAME = "Unknown Artist";

    public ArrayList<Album> albums;

    public Artist() {
        this.albums = new ArrayList<>();
    }

    public Artist(ArrayList<Album> albums) {
        this.albums = new ArrayList<>(albums);
    }

    public int getId() {
        return safeGetFirstAlbum().getArtistId();
    }

    public String getName() {
        String name = safeGetFirstAlbum().getArtist();
        if (name.equals("<unknown>")) {
            return UNKNOWN_ARTIST_DISPLAY_NAME;
        }
        return name;
    }

    public int getNumberOfTracks() {
        int songCount = 0;
        for (Album album : albums) {
            songCount += album.getSongCount();
        }
        return songCount;
    }

    public int getNumberOfAlbums() {
        return albums.size();
    }

    public Album safeGetFirstAlbum() {
        return albums.isEmpty() ? new Album() : albums.get(0);
    }

    @Override
    public int hashCode() {
        return this.getId();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ByteBuffer.allocate(Integer.SIZE).putInt(hashCode()).array());
    }

    @Override
    public boolean equals(@NonNull Object other) {
        return other instanceof Artist && this.getId() == ((Artist) other).getId();
    }

    @NonNull
    @Override
    public String toString() {
        return this.getName();
    }
}
