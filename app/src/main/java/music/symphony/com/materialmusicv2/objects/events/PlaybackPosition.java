package music.symphony.com.materialmusicv2.objects.events;

public class PlaybackPosition {
    public int position;
    public int duration;

    public PlaybackPosition(int position, int duration) {
        this.position = position;
        this.duration = duration;
    }
}
