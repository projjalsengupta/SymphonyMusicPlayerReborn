package music.symphony.com.materialmusicv2.objects.events;

public class MediaStates {
    public boolean shuffle;
    public int repeat;
    public boolean playbackState;

    public MediaStates(boolean shuffle, int repeat, boolean playbackState) {
        this.shuffle = shuffle;
        this.repeat = repeat;
        this.playbackState = playbackState;
    }
}
