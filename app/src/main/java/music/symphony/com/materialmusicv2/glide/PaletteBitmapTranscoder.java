package music.symphony.com.materialmusicv2.glide;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.getDominantColors;

public class PaletteBitmapTranscoder implements ResourceTranscoder<Bitmap, PaletteBitmap> {
    private final BitmapPool bitmapPool;

    public PaletteBitmapTranscoder(@NonNull BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Nullable
    @Override
    public Resource<PaletteBitmap> transcode(@NonNull Resource<Bitmap> toTranscode, @NonNull Options options) {
        Bitmap bitmap = toTranscode.get();
        int[] colors = getDominantColors(bitmap);
        PaletteBitmap result = new PaletteBitmap(bitmap, colors[0], colors[1]);
        return new PaletteBitmapResource(result, bitmapPool);
    }
}