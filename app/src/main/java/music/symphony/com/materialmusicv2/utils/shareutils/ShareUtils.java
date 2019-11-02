package music.symphony.com.materialmusicv2.utils.shareutils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;

import music.symphony.com.materialmusicv2.R;

public class ShareUtils {
    public static void shareSong(Context context, String sharePath) {
        if (context == null || sharePath == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileProvider", new File(sharePath)));
            share.setType("*/*");
            context.startActivity(Intent.createChooser(share, context.getString(R.string.share_song_using)));
        } else {
            File file = new File(sharePath);
            Uri uri = Uri.fromFile(file);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(Intent.createChooser(share, context.getString(R.string.share_song_using)));
        }
    }
}
