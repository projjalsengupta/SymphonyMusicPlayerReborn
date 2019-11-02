package music.symphony.com.materialmusicv2.objects.events;

public class MediaSessionData {
    public AlbumArt albumArt;
    public MediaStates mediaStates;
    public CurrentPlayingSong currentPlayingSong;

    public MediaSessionData(AlbumArt albumArt, MediaStates mediaStates, CurrentPlayingSong currentPlayingSong) {
        this.albumArt = albumArt;
        this.mediaStates = mediaStates;
        this.currentPlayingSong = currentPlayingSong;
    }
}
