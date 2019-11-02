package music.symphony.com.materialmusicv2.widgets;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.MainActivity;
import music.symphony.com.materialmusicv2.objects.events.MediaSessionData;
import music.symphony.com.materialmusicv2.objects.events.MediaStates;
import music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;

class SmallestColoredWidgetController extends WidgetPusher {

    private Context context;

    private RemoteViews widgetContentView;

    SmallestColoredWidgetController(Context context) {
        this.context = context;
        widgetContentView = new RemoteViews(context.getPackageName(), R.layout.app_widget_smallest_colored);
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        widgetContentView.setOnClickPendingIntent(R.id.widgetBackground, configPendingIntent);
        pushWidget(context, widgetContentView, MusicWidgetProviderSmallestColored.class);
    }

    void onMetadataChanged(MediaSessionData mediaSessionData) {
        AsyncTask.execute(() -> {
            try {
                int backgroundColor = mediaSessionData.albumArt.backgroundColor;
                int foregroundColor = mediaSessionData.albumArt.foregroundColor;
                widgetContentView = new RemoteViews(context.getPackageName(), R.layout.app_widget_smallest_colored);
                setColorFilter(widgetContentView, backgroundColor, R.id.background);
                setTextColor(widgetContentView, foregroundColor,
                        R.id.songName,
                        R.id.artistName
                );
                setColorFilter(widgetContentView, foregroundColor,
                        R.id.playPrevious,
                        R.id.playPause,
                        R.id.playNext
                );
                setListeners(context, widgetContentView);
                int maxWidth = (int) context.getResources().getDisplayMetrics().density * 70;
                int maxHeight = (int) context.getResources().getDisplayMetrics().density * 70;
                widgetContentView.setImageViewBitmap(R.id.albumArt, BitmapUtils.getRoundedCornerBitmap(
                        BitmapUtils.resizeBitmap(mediaSessionData.albumArt.albumArt, maxWidth, maxHeight),
                        8,
                        context,
                        false,
                        true,
                        false,
                        true));
                widgetContentView.setTextViewText(R.id.songName, mediaSessionData.currentPlayingSong.songName);
                widgetContentView.setTextViewText(R.id.albumName, mediaSessionData.currentPlayingSong.albumName);
                widgetContentView.setTextViewText(R.id.artistName, mediaSessionData.currentPlayingSong.artistName);
                widgetContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(mediaSessionData.mediaStates.playbackState));
                pushWidget(context, widgetContentView, MusicWidgetProviderSmallestColored.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    void onActionsChanged(MediaStates mediaStates) {
        AsyncTask.execute(() -> {
            try {
                widgetContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(mediaStates.playbackState));
                pushWidget(context, widgetContentView, MusicWidgetProviderSmallestColored.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    void reset() {
        AsyncTask.execute(() -> {
            try {
                widgetContentView = new RemoteViews(context.getPackageName(), R.layout.app_widget_smallest_colored);
                Intent configIntent = new Intent(context, MainActivity.class);
                PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
                widgetContentView.setOnClickPendingIntent(R.id.widgetBackground, configPendingIntent);
                pushWidget(context, widgetContentView, MusicWidgetProviderSmallestColored.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
