package music.symphony.com.materialmusicv2.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.greenrobot.eventbus.EventBus;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.MainActivity;
import music.symphony.com.materialmusicv2.objects.events.MediaSessionData;

public class MusicWidgetProviderBig extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        RemoteViews widgetContentView = new RemoteViews(context.getPackageName(), R.layout.app_widget_big);
        Intent configIntent = new Intent(context, MainActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        widgetContentView.setOnClickPendingIntent(R.id.widgetBackground, configPendingIntent);
        try {
            ComponentName componentNameBigWidget = new ComponentName(context, MusicWidgetProviderBig.class);
            AppWidgetManager.getInstance(context).updateAppWidget(componentNameBigWidget, widgetContentView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        try {
            SymphonyApplication.getInstance().getWidgetUtils().getBigWidgetController().onMetadataChanged(EventBus.getDefault().getStickyEvent(MediaSessionData.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}