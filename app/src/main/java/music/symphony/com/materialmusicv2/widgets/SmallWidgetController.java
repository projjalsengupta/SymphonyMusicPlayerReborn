package music.symphony.com.materialmusicv2.widgets;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.MainActivity;
import music.symphony.com.materialmusicv2.objects.events.MediaSessionData;
import music.symphony.com.materialmusicv2.objects.events.MediaStates;
import music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.resizeBitmap;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;

class SmallWidgetController extends WidgetPusher {

    private Context context;

    private static RemoteViews widgetContentView;

    SmallWidgetController(Context context) {
        this.context = context;
        widgetContentView = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        widgetContentView.setOnClickPendingIntent(R.id.widgetBackground, configPendingIntent);
        pushWidget(context, widgetContentView, MusicWidgetProvider.class);
    }

    void onMetadataChanged(MediaSessionData mediaSessionData) {
        AsyncTask.execute(() -> {
            try {
                int tintColor = ContrastColor(mediaSessionData.albumArt.backgroundColor) == Color.WHITE ? mediaSessionData.albumArt.foregroundColor : mediaSessionData.albumArt.backgroundColor;
                widgetContentView = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                setListeners(context, widgetContentView);
                int pixels = (int) (context.getResources().getDisplayMetrics().widthPixels / 1.5);
                widgetContentView.setImageViewBitmap(R.id.albumArt, resizeBitmap(mediaSessionData.albumArt.albumArt, pixels, pixels));
                widgetContentView.setTextViewText(R.id.songName, mediaSessionData.currentPlayingSong.songName);
                widgetContentView.setTextViewText(R.id.albumName, mediaSessionData.currentPlayingSong.albumName);
                widgetContentView.setTextViewText(R.id.artistName, mediaSessionData.currentPlayingSong.artistName);
                setTextColor(widgetContentView, tintColor, R.id.albumName, R.id.artistName);
                setColorFilter(widgetContentView, tintColor, R.id.circle);
                setColorFilter(widgetContentView, tintColor, R.id.playPause);
                widgetContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(mediaSessionData.mediaStates.playbackState));
                widgetContentView.setImageViewResource(R.id.repeat, DrawableUtils.getRepeatResourceForWidget(mediaSessionData.mediaStates.repeat));
                widgetContentView.setInt(R.id.shuffle, "setAlpha", mediaSessionData.mediaStates.shuffle ? 255 : 128);
                widgetContentView.setInt(R.id.repeat, "setAlpha", mediaSessionData.mediaStates.repeat == PlaybackStateCompat.REPEAT_MODE_NONE ? 128 : 255);
                pushWidget(context, widgetContentView, MusicWidgetProvider.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    void onActionsChanged(MediaStates mediaStates) {
        AsyncTask.execute(() -> {
            try {
                widgetContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(mediaStates.playbackState));
                widgetContentView.setImageViewResource(R.id.repeat, DrawableUtils.getRepeatResourceForWidget(mediaStates.repeat));
                widgetContentView.setInt(R.id.shuffle, "setAlpha", mediaStates.shuffle ? 255 : 128);
                widgetContentView.setInt(R.id.repeat, "setAlpha", mediaStates.repeat == PlaybackStateCompat.REPEAT_MODE_NONE ? 128 : 255);
                pushWidget(context, widgetContentView, MusicWidgetProvider.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    void reset() {
        AsyncTask.execute(() -> {
            try {
                widgetContentView = new RemoteViews(context.getPackageName(), R.layout.app_widget);
                Intent configIntent = new Intent(context, MainActivity.class);
                PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
                widgetContentView.setOnClickPendingIntent(R.id.widgetBackground, configPendingIntent);
                pushWidget(context, widgetContentView, MusicWidgetProvider.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
