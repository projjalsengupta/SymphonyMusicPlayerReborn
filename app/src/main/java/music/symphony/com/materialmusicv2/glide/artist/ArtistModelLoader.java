package music.symphony.com.materialmusicv2.glide.artist;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.nio.ByteBuffer;

import music.symphony.com.materialmusicv2.objects.Artist;

public final class ArtistModelLoader implements ModelLoader<Artist, ByteBuffer> {

    ArtistModelLoader() {
    }

    @NonNull
    @Override
    public LoadData<ByteBuffer> buildLoadData(@NonNull Artist model, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(model), new ArtistDataFetcher(model));
    }

    @Override
    public boolean handles(@NonNull Artist model) {
        return true;
    }
}