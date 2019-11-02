package music.symphony.com.materialmusicv2.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.MainActivity;

import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_NEXT;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_PAUSE_OR_RESUME;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_PREVIOUS;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_REPEAT;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_SHUFFLE;

class WidgetPusher {

    static void pushWidget(Context context, RemoteViews remoteViews, Class<?> widgetProvider) {
        try {
            ComponentName componentNameSmallWidget = new ComponentName(context, widgetProvider);
            AppWidgetManager.getInstance(context).updateAppWidget(componentNameSmallWidget, remoteViews);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setListeners(Context context, RemoteViews remoteViews) {
        Intent playPreviousIntent = new Intent(NOTIFY_PREVIOUS);
        Intent pauseOrResumeIntent = new Intent(NOTIFY_PAUSE_OR_RESUME);
        Intent playNextIntent = new Intent(NOTIFY_NEXT);
        Intent shuffleIntent = new Intent(NOTIFY_SHUFFLE);
        Intent repeatIntent = new Intent(NOTIFY_REPEAT);
        PendingIntent playPrevious = PendingIntent.getBroadcast(context, 0, playPreviousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.playPrevious, playPrevious);
        PendingIntent playOrPause = PendingIntent.getBroadcast(context, 0, pauseOrResumeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.playPause, playOrPause);
        PendingIntent pNext = PendingIntent.getBroadcast(context, 0, playNextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.playNext, pNext);
        PendingIntent pShuffle = PendingIntent.getBroadcast(context, 0, shuffleIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.shuffle, pShuffle);
        PendingIntent pRepeat = PendingIntent.getBroadcast(context, 0, repeatIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.repeat, pRepeat);
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.widgetBackground, configPendingIntent);
    }
}
