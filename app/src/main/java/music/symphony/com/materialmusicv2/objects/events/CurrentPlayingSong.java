package music.symphony.com.materialmusicv2.objects.events;

public class CurrentPlayingSong {
    public String songName;
    public String artistName;
    public String albumName;

    public CurrentPlayingSong(String songName, String artistName, String albumName) {
        this.songName = songName;
        this.artistName = artistName;
        this.albumName = albumName;
    }
}
