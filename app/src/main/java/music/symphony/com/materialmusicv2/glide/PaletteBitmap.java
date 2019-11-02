package music.symphony.com.materialmusicv2.glide;

import android.graphics.Bitmap;

public class PaletteBitmap {
    public final int backgroundColor;
    public final int foregroundColor;
    public final Bitmap bitmap;

    public PaletteBitmap(Bitmap bitmap, int backgroundColor, int foregroundColor) {
        this.bitmap = bitmap;
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }
}