package music.symphony.com.materialmusicv2.glide.song;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;

import java.nio.ByteBuffer;

import music.symphony.com.materialmusicv2.objects.Song;

public final class SongModelLoader implements ModelLoader<Song, ByteBuffer> {

    private Context context;

    SongModelLoader(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public LoadData<ByteBuffer> buildLoadData(@NonNull Song model, int width, int height, Options options) {
        return new LoadData<>(new ObjectKey(model), new SongDataFetcher(model, context));
    }

    @Override
    public boolean handles(@NonNull Song model) {
        return true;
    }
}