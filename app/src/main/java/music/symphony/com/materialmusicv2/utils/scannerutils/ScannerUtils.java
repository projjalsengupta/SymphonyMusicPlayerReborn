package music.symphony.com.materialmusicv2.utils.scannerutils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class ScannerUtils {
    public static void callBroadCast(String path, final Context context) {
        try {
            if (path == null || context == null) {
                return;
            }
            MediaScannerConnection.scanFile(context, new String[]{path},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            context.getContentResolver()
                                    .delete(uri, null, null);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
