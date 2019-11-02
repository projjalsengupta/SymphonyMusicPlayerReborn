package music.symphony.com.materialmusicv2.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Objects;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.BuyProActivity;

import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_PLAY_SONG_AT;

public class QueueWidgetProvider extends AppWidgetProvider {

    public static final String NOTIFY_QUEUE_ITEM_CLICK = "music.symphony.com.materialmusicv2.playsongatwidgetonreceive";
    public static final String EXTRA_ITEM = "music.symphony.com.materialmusicv2.queuewidgetprovider.extraitem";

    private static RemoteViews views = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    ComponentName componentName = new ComponentName(context, QueueWidgetProvider.class);
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.queue);
                } else if (action.equals(QueueWidgetProvider.NOTIFY_QUEUE_ITEM_CLICK)) {
                    Intent playIntent = new Intent(NOTIFY_PLAY_SONG_AT);
                    playIntent.putExtra("position", Objects.requireNonNull(intent.getExtras()).getInt(QueueWidgetProvider.EXTRA_ITEM));
                    context.sendBroadcast(playIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onReceive(context, intent);
    }

    public static void sendRefreshBroadcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, QueueWidgetProvider.class));
        context.sendBroadcast(intent);
    }

    public static void scroll() {
        AsyncTask.execute(() -> {
            try {
                ComponentName componentNameQueueWidget = new ComponentName(SymphonyApplication.getInstance().getApplicationContext(), QueueWidgetProvider.class);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(SymphonyApplication.getInstance().getApplicationContext());
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentNameQueueWidget);
                for (int appWidgetId : appWidgetIds) {
                    views.setScrollPosition(R.id.queue, SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition());
                    appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.queue_widget_layout
            );

            Intent buyProIntent = new Intent(context, BuyProActivity.class);
            PendingIntent buyProPendingIntent = PendingIntent.getActivity(context, 0, buyProIntent, 0);
            views.setOnClickPendingIntent(R.id.proText, buyProPendingIntent);

            if (SymphonyApplication.getInstance().getPreferenceUtils().getDonated()) {
                views.setViewVisibility(R.id.proText, View.GONE);
            } else {
                views.setViewVisibility(R.id.proText, View.VISIBLE);
            }

            Intent intent = new Intent(context, QueueWidgetRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            views.setScrollPosition(R.id.queue, SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition());
            views.setRemoteAdapter(R.id.queue, intent);

            Intent onClickIntent = new Intent(context, QueueWidgetProvider.class);
            onClickIntent.setAction(QueueWidgetProvider.NOTIFY_QUEUE_ITEM_CLICK);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.queue, onClickPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
