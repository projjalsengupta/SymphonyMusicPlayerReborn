package music.symphony.com.materialmusicv2.widgets;

/**
 * Created by projjal sengupta on 23-01-2018.
 */

import android.content.Intent;
import android.widget.RemoteViewsService;

public class QueueWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new QueueWidgetRemoteViewsFactory(this.getApplicationContext());
    }
}