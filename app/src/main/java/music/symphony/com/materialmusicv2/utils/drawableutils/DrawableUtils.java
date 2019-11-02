package music.symphony.com.materialmusicv2.utils.drawableutils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import music.symphony.com.materialmusicv2.R;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;

public class DrawableUtils {

    public static int getRepeatResourceBlack(int state) {
        switch (state) {
            case PlaybackStateCompat.REPEAT_MODE_ALL: {
                return R.drawable.ic_repeat_black_24dp;
            }
            case PlaybackStateCompat.REPEAT_MODE_ONE: {
                return R.drawable.ic_repeat_one_black_24dp;
            }
            case PlaybackStateCompat.REPEAT_MODE_NONE: {
                return R.drawable.ic_repeat_black_transparent_24dp;
            }
            default: {
                return R.drawable.ic_repeat_black_24dp;
            }
        }
    }

    public static int getRepeatResourceForWidget(int state) {
        switch (state) {
            case PlaybackStateCompat.REPEAT_MODE_ALL: {
                return R.drawable.ic_repeat;
            }
            case PlaybackStateCompat.REPEAT_MODE_ONE: {
                return R.drawable.ic_repeat_one;
            }
            case PlaybackStateCompat.REPEAT_MODE_NONE: {
                return R.drawable.ic_repeat;
            }
            default: {
                return R.drawable.ic_repeat;
            }
        }
    }

    public static int getRepeatResourceWhite(int state) {
        switch (state) {
            case PlaybackStateCompat.REPEAT_MODE_ALL: {
                return R.drawable.ic_repeat_white_24dp;
            }
            case PlaybackStateCompat.REPEAT_MODE_ONE: {
                return R.drawable.ic_repeat_one_white_24dp;
            }
            case PlaybackStateCompat.REPEAT_MODE_NONE: {
                return R.drawable.ic_repeat_white_transparent_24dp;
            }
            default: {
                return R.drawable.ic_repeat_white_24dp;
            }
        }
    }

    public static int getPlayPauseResourceBlack(boolean isPlaying) {
        if (isPlaying) {
            return R.drawable.ic_pause_black_24dp;
        } else {
            return R.drawable.ic_play_arrow_black_24dp;
        }
    }

    public static int getPlayPauseResourceForNotificationAndWidget(boolean isPlaying) {
        if (isPlaying) {
            return R.drawable.ic_pause;
        } else {
            return R.drawable.ic_play;
        }
    }

    public static int getFavoriteResourceForNotification(boolean favorite) {
        return favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border;
    }

    public static int getPlayPauseResourceWhite(boolean isPlaying) {
        if (isPlaying) {
            return R.drawable.ic_pause_white_24dp;
        } else {
            return R.drawable.ic_play_arrow_white_24dp;
        }
    }

}
