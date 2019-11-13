package music.symphony.com.materialmusicv2;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.nio.ByteBuffer;

import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.glide.PaletteBitmapTranscoder;
import music.symphony.com.materialmusicv2.glide.artist.ArtistLoaderFactory;
import music.symphony.com.materialmusicv2.glide.song.SongLoaderFactory;
import music.symphony.com.materialmusicv2.objects.Artist;
import music.symphony.com.materialmusicv2.objects.Song;

@GlideModule
public final class MyAppGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);

        registry.prepend(Song.class, ByteBuffer.class, new SongLoaderFactory(context));
        registry.prepend(Artist.class, ByteBuffer.class, new ArtistLoaderFactory());

        registry.register(Bitmap.class, PaletteBitmap.class, new PaletteBitmapTranscoder(glide.getBitmapPool()));
    }

    @Override
    public void applyOptions(@NonNull Context context, GlideBuilder builder) {
        long diskCacheSizeBytes = 1024L * 1024L * 2048L; //2 GB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        int bitmapPoolSizeBytes = 1024 * 1024 * 10; //10MB
        builder.setBitmapPool(new LruBitmapPool(bitmapPoolSizeBytes));
        builder.setDefaultRequestOptions(
                new RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .disallowHardwareConfig());
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}