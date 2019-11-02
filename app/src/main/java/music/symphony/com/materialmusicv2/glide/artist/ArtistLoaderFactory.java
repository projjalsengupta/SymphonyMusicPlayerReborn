package music.symphony.com.materialmusicv2.glide.artist;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.nio.ByteBuffer;

import music.symphony.com.materialmusicv2.objects.Artist;

public class ArtistLoaderFactory implements ModelLoaderFactory<Artist, ByteBuffer> {

    public ArtistLoaderFactory() {
    }

    @NonNull
    @Override
    public ModelLoader<Artist, ByteBuffer> build(@NonNull MultiModelLoaderFactory unused) {
        return new ArtistModelLoader();
    }

    @Override
    public void teardown() {
    }
}
