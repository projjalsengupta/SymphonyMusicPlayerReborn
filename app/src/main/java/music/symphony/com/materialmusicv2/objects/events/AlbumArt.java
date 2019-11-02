package music.symphony.com.materialmusicv2.objects.events;

import android.graphics.Bitmap;

public class AlbumArt {
    public Bitmap albumArt;
    public int backgroundColor;
    public int foregroundColor;

    public AlbumArt(Bitmap albumArt, int backgroundColor, int foregroundColor) {
        this.albumArt = albumArt;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }
}
