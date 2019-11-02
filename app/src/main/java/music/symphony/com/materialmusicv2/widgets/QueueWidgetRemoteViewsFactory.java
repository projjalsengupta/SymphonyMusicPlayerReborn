package music.symphony.com.materialmusicv2.widgets;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.InputStream;
import java.util.ArrayList;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.symphony.com.materialmusicv2.utils.misc.Statics;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.getBytes;

public class QueueWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private ArrayList<Song> songs;

    QueueWidgetRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        try {
            songs = new ArrayList<>(SymphonyApplication.getInstance().getPlayingQueueManager().getSongs());
        } catch (Exception e) {
            songs = new ArrayList<>();
        }
    }

    @Override
    public void onDestroy() {
        if (songs != null) {
            songs.clear();
            songs = null;
        }
    }

    @Override
    public int getCount() {
        return songs == null ? 0 : songs.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                songs == null) {
            return null;
        }

        RemoteViews mView = new RemoteViews(mContext.getPackageName(),
                R.layout.queue_widget_item);
        mView.setTextViewText(R.id.songName, songs.get(position).getName());
        mView.setTextViewText(R.id.songArtist, songs.get(position).getArtist());
        mView.setTextColor(R.id.songName, Color.WHITE);
        mView.setTextColor(R.id.songArtist, Color.WHITE);

        byte[] data = getByteDataFromFile(songs.get(position).getPath());
        if (data == null) {
            data = getByteDataFromUri(songs.get(position).getAlbumId(), mContext);
        }
        if (data != null) {
            int pixels = (int) (Resources.getSystem().getDisplayMetrics().density * 40);
            Bitmap bitmap = BitmapUtils.decodeByteArray(data, data.length, pixels, pixels, Bitmap.Config.RGB_565);
            if (bitmap != null) {
                mView.setImageViewBitmap(R.id.albumArt, BitmapUtils.getRoundedCornerBitmap(
                        bitmap,
                        8,
                        mContext,
                        false,
                        false,
                        false,
                        false
                ));
            } else {
                mView.setImageViewBitmap(R.id.albumArt, bitmap);
            }
        } else {
            mView.setImageViewResource(R.id.albumArt, R.drawable.ic_blank_album_art);
        }

        Bundle extras = new Bundle();
        extras.putInt(QueueWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        mView.setOnClickFillInIntent(R.id.songItemBackground, fillInIntent);

        return mView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return songs.get(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
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

    private byte[] getByteDataFromUri(long artLong, Context context) {
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
}