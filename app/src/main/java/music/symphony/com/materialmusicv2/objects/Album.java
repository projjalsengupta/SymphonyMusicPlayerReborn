package music.symphony.com.materialmusicv2.objects;

import java.util.ArrayList;

public class Album {

    private ArrayList<Song> songs;

    public Album() {
        this.songs = new ArrayList<>();
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public long getId() {
        if (songs == null || songs.size() == 0) {
            return -1;
        } else {
            return songs.get(0).getAlbumId();
        }
    }

    public String getName() {
        if (songs == null || songs.size() == 0) {
            return "-";
        } else {
            return songs.get(0).getAlbum();
        }
    }

    public String getArtist() {
        if (songs == null || songs.size() == 0) {
            return "-";
        } else {
            return songs.get(0).getArtist();
        }
    }

    public int getSongCount() {
        return (songs == null || songs.isEmpty()) ? 0 : songs.size();
    }

    public int getArtistId() {
        return (songs == null || songs.isEmpty()) ? -1 : (int) songs.get(0).getArtistId();
    }

    public int getYear() {
        return (songs == null || songs.isEmpty()) ? -1 : (int) songs.get(0).getYear();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Album && this.getId() == ((Album) other).getId();
    }
}
