package music.symphony.com.materialmusicv2.widgets;

import android.content.Context;
import android.os.AsyncTask;

import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.events.MediaSessionData;
import music.symphony.com.materialmusicv2.objects.events.MediaStates;
import music.symphony.com.materialmusicv2.objects.events.PlaybackPosition;

public class WidgetUtils {

    public WidgetUtils(Context context) {
        bigWidgetController = new BigWidgetController(context);
        smallWidgetController = new SmallWidgetController(context);
        smallestColoredWidgetController = new SmallestColoredWidgetController(context);
    }

    BigWidgetController getBigWidgetController() {
        return bigWidgetController;
    }

    SmallWidgetController getSmallWidgetController() {
        return smallWidgetController;
    }

    SmallestColoredWidgetController getSmallestColoredWidgetController() {
        return smallestColoredWidgetController;
    }

    private BigWidgetController bigWidgetController;
    private SmallWidgetController smallWidgetController;
    private SmallestColoredWidgetController smallestColoredWidgetController;

    public void onMetadataChanged(MediaSessionData mediaSessionData) {
        if (bigWidgetController != null) {
            bigWidgetController.onMetadataChanged(mediaSessionData);
        }
        if (smallWidgetController != null) {
            smallWidgetController.onMetadataChanged(mediaSessionData);
        }
        if (smallestColoredWidgetController != null) {
            smallestColoredWidgetController.onMetadataChanged(mediaSessionData);
        }
    }

    public void onActionsChanged(MediaStates mediaStates) {
        if (bigWidgetController != null) {
            bigWidgetController.onActionsChanged(mediaStates);
        }
        if (smallWidgetController != null) {
            smallWidgetController.onActionsChanged(mediaStates);
        }
        if (smallestColoredWidgetController != null) {
            smallestColoredWidgetController.onActionsChanged(mediaStates);
        }
    }

    public void onPlaybackPositionChanged(PlaybackPosition playbackPosition) {
        if (bigWidgetController != null) {
            bigWidgetController.onPlaybackPositionChanged(playbackPosition.position, playbackPosition.duration);
        }
    }

    public void nullify() {
        if (smallWidgetController != null) {
            smallWidgetController.reset();
            smallWidgetController = null;
        }
        if (bigWidgetController != null) {
            bigWidgetController.reset();
            bigWidgetController = null;
        }
        if (smallestColoredWidgetController != null) {
            smallestColoredWidgetController.reset();
            smallestColoredWidgetController = null;
        }
    }

    public void onSongListChanged() {
        AsyncTask.execute(() -> {
            QueueWidgetProvider.sendRefreshBroadcast(SymphonyApplication.getInstance().getApplicationContext());
            QueueWidgetProvider.scroll();
        });
    }

    public void onPositionChanged() {
        AsyncTask.execute(QueueWidgetProvider::scroll);
    }
}
