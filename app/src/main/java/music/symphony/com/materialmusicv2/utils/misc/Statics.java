package music.symphony.com.materialmusicv2.utils.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import music.symphony.com.materialmusicv2.SymphonyApplication;

public class Statics {

    public static final String RELOAD_LIBRARY_INTENT = "music.symphony.com.materialmusicv2.reloadlibrary";
    public static final int NOTIFICATION_ID = 1111;
    public static final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
    public static final String NOTIFY_PREVIOUS = "music.symphony.com.materialmusicv2.previous";
    public static final String NOTIFY_STOP = "music.symphony.com.materialmusicv2.stop";
    public static final String NOTIFY_FAVORITE = "music.symphony.com.materialmusicv2.favorite";
    public static final String NOTIFY_PAUSE_OR_RESUME = "music.symphony.com.materialmusicv2.pauseOrResume";
    public static final String NOTIFY_NEXT = "music.symphony.com.materialmusicv2.next";
    public static final String NOTIFY_SHUFFLE = "music.symphony.com.materialmusicv2.shuffle";
    public static final String NOTIFY_REPEAT = "music.symphony.com.materialmusicv2.repeat";
    public static final String NOTIFY_SKIP2SONGS = "music.symphony.com.materialmusicv2.skip2songs";
    public static final String NOTIFY_SKIP3SONGS = "music.symphony.com.materialmusicv2.skip3songs";
    public static final String NOTIFY_PLAY_SONG_AT = "music.symphony.com.materialmusicv2.playsongat";
    public static final String CHANNEL_MEDIA_CONTROLS = "symphony_media_controls";

    public static Uri treeUri = null;

    public static Uri getTreeUri(Activity activity) {
        if (treeUri == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.startActivityForResult(intent, 42);
            }
            return null;
        } else {
            return treeUri;
        }
    }

    public static void loadTreeUri(Context context) {
        String treeUriString = SymphonyApplication.getInstance().getPreferenceUtils().getTreeUri();
        if (treeUriString != null) {
            treeUri = Uri.parse(treeUriString);
            context.grantUriPermission(context.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            context.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            treeUri = null;
        }
    }
}