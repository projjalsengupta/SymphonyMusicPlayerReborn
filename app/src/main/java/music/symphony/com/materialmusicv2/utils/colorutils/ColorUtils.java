package music.symphony.com.materialmusicv2.utils.colorutils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ColorUtils {

    public static void setColorFilter(int color, ImageButton... imageButtons) {
        for (ImageButton imageButton : imageButtons) {
            if (imageButton != null) {
                imageButton.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public static void changeTextInputLayoutHintColor(int color, TextInputLayout... textInputLayouts) {
        for (TextInputLayout til : textInputLayouts) {
            if (til != null) {
                til.setHintTextColor(new ColorStateList(
                        new int[][]{
                                new int[]{android.R.attr.state_enabled},
                                new int[]{android.R.attr.state_focused}
                        },
                        new int[]{
                                color,
                                color
                        }
                ));
            }
        }
    }

    public static void setColorFilter(int color, ExtendedFloatingActionButton... floatingActionButtons) {
        for (ExtendedFloatingActionButton fab : floatingActionButtons) {
            if (fab != null) {
                fab.setIconTint(ColorStateList.valueOf(color));
                fab.setTextColor(color);
            }
        }
    }

    public static void setColorFilter(int color, ImageView... imageViews) {
        for (ImageView imageView : imageViews) {
            if (imageView != null) {
                imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public static void setTextColor(int color, TextView... textViews) {
        for (TextView textView : textViews) {
            if (textView != null) {
                textView.setTextColor(color);
            }
        }
    }

    public static void setBackgroundColor(int[] colors, View[] views) {
        if (colors != null && views != null) {
            if (colors.length != views.length) {
                return;
            }
            for (int i = 0; i < views.length; i++) {
                if (views[i] != null) {
                    views[i].setBackgroundColor(colors[i]);
                }
            }
        }
    }

    public static void setColorFilter(int color, SeekBar... seekBars) {
        for (SeekBar seekBar : seekBars) {
            if (seekBar != null) {
                if (seekBar.getProgressDrawable() != null) {
                    seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP);
                }
                if (seekBar.getThumb() != null) {
                    seekBar.getThumb().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    }

    public static void setColorFilter(int color, MaterialProgressBar... materialProgressBars) {
        for (MaterialProgressBar materialProgressBar : materialProgressBars) {
            if (materialProgressBar != null) {
                materialProgressBar.setSupportProgressTintList(ColorStateList.valueOf(color));
            }
        }
    }

    public static void setColorFilter(int colorProgress, int colorSecondaryProgress, MaterialProgressBar... materialProgressBars) {
        for (MaterialProgressBar materialProgressBar : materialProgressBars) {
            if (materialProgressBar != null) {
                materialProgressBar.setSupportProgressTintList(ColorStateList.valueOf(colorProgress));
                materialProgressBar.setSupportProgressBackgroundTintList(ColorStateList.valueOf(adjustAlpha(colorSecondaryProgress, 0.25f)));
            }
        }
    }

    public static void setBackgroundColorFilter(int color, View... views) {
        for (View view : views) {
            if (view != null) {
                view.getBackground().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static void setColorFilter(RemoteViews remoteView, int color, int... ids) {
        if (remoteView == null) {
            return;
        }
        for (int id : ids) {
            remoteView.setInt(id, "setColorFilter", color);
        }
    }

    public static void setTextColor(RemoteViews remoteView, int color, int... ids) {
        if (remoteView == null) {
            return;
        }
        for (int id : ids) {
            remoteView.setInt(id, "setTextColor", color);
        }
    }

    public static double compareColors(int firstColor, int secondColor) {
        double firstQueryColor = 1 - (0.299 * Color.red(firstColor) + 0.587 * Color.green(firstColor) + 0.114 * Color.blue(firstColor)) / 256;
        double secondQueryColor = 1 - (0.299 * Color.red(secondColor) + 0.587 * Color.green(secondColor) + 0.114 * Color.blue(secondColor)) / 256;
        return Math.abs(firstQueryColor - secondQueryColor);
    }

    public static int ContrastColor(int color) {
        final int finalColor;
        double queryColor = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 256;
        if (queryColor < 0.5) {
            finalColor = Color.BLACK;
        } else {
            finalColor = Color.WHITE;
        }
        return finalColor;
    }

    public static int lighten(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = lightenColor(red, fraction);
        green = lightenColor(green, fraction);
        blue = lightenColor(blue, fraction);
        int alpha = Color.alpha(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static int darken(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darkenColor(red, fraction);
        green = darkenColor(green, fraction);
        blue = darkenColor(blue, fraction);
        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    private static int darkenColor(int color, double fraction) {
        return (int) Math.max(color - (color * fraction), 0);
    }

    private static int lightenColor(int color, double fraction) {
        return (int) Math.min(color + (color * fraction), 255);
    }

}
