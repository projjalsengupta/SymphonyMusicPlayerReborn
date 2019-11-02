package music.symphony.com.materialmusicv2.utils.broadcastutils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import music.symphony.com.materialmusicv2.objects.Song;

public class BroadcastUtils {
    public static void startBroadcast(Song song, Context context, int position) {
        try {
            if (context == null || song == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction("com.android.music.metachanged");
            Bundle bundle = new Bundle();
            bundle.putString("track", song.getName());
            bundle.putString("artist", song.getArtist());
            bundle.putString("album", song.getAlbum());
            bundle.putInt("duration", song.getDuration());
            bundle.putInt("position", position);
            bundle.putBoolean("playing", true);
            bundle.putString("scrobbling_source", "music.symphony.com.materialmusicv2");
            intent.putExtras(bundle);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pauseBroadcast(Song song, Context context, int position) {
        try {
            if (context == null || song == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction("com.android.music.metachanged");
            Bundle bundle = new Bundle();
            bundle.putString("track", song.getName());
            bundle.putString("artist", song.getArtist());
            bundle.putString("album", song.getAlbum());
            bundle.putInt("duration", song.getDuration());
            bundle.putInt("position", position);
            bundle.putBoolean("playing", false);
            bundle.putString("scrobbling_source", "music.symphony.com.materialmusicv2");
            intent.putExtras(bundle);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
