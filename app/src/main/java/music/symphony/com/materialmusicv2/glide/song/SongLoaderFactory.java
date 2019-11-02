package music.symphony.com.materialmusicv2.glide.song;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.nio.ByteBuffer;

import music.symphony.com.materialmusicv2.objects.Song;

public class SongLoaderFactory implements ModelLoaderFactory<Song, ByteBuffer> {

    private Context context;

    public SongLoaderFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<Song, ByteBuffer> build(@NonNull MultiModelLoaderFactory unused) {
        return new SongModelLoader(context);
    }

    @Override
    public void teardown() {
    }
}
