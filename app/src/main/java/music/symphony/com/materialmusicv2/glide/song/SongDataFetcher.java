package music.symphony.com.materialmusicv2.glide.song;

import android.content.ContentUris;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.InputStream;
import java.nio.ByteBuffer;

import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.misc.Statics;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.getBytes;

public class SongDataFetcher implements DataFetcher<ByteBuffer> {

    private final Song model;
    private Context context;

    SongDataFetcher(Song model, Context context) {
        this.model = model;
        this.context = context;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super ByteBuffer> callback) {
        byte[] data = getByteDataFromFile(model.getPath());
        if (data == null) {
            data = getByteDataFromUri(model.getAlbumId());
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        callback.onDataReady(byteBuffer);
    }

    private byte[] getByteDataFromFile(String path) {
        if (path == null) {
            return null;
        }
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            byte[] artworkData = mmr.getEmbeddedPicture();
            mmr.release();
            return artworkData;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] getByteDataFromUri(long artLong) {
        try {
            Uri uri = ContentUris.withAppendedId(Statics.artworkUri, artLong);
            try (InputStream iStream = context.getContentResolver().openInputStream(uri)) {
                if (iStream != null) {
                    return getBytes(iStream);
                }
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void cleanup() {
        context = null;
    }

    @Override
    public void cancel() {
        context = null;
    }

    @NonNull
    @Override
    public Class<ByteBuffer> getDataClass() {
        return ByteBuffer.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}
