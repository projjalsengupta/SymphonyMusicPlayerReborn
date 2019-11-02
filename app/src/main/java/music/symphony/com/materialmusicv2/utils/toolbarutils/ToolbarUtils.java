package music.symphony.com.materialmusicv2.utils.toolbarutils;

import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;

public class ToolbarUtils {
    public static void setUpToolbar(Toolbar toolbar, int title, int[] toolbarIcons, int backgroundColor, AppCompatActivity activity, Runnable runnable) {
        if (toolbar == null || activity == null || toolbarIcons == null || toolbarIcons.length < 2) {
            return;
        }
        toolbar.setTitle(title);
        int contrastColorBackground = ContrastColor(backgroundColor);
        toolbar.setBackgroundColor(backgroundColor);
        toolbar.setTitleTextColor(contrastColorBackground);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(contrastColorBackground == Color.BLACK ? toolbarIcons[0] : toolbarIcons[1]);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(view -> runnable.run());
    }

    public static void setUpToolbar(Toolbar toolbar, String title, int[] toolbarIcons, int backgroundColor, AppCompatActivity activity, Runnable runnable) {
        if (toolbar == null || activity == null || toolbarIcons == null || toolbarIcons.length < 2) {
            return;
        }
        toolbar.setTitle(title);
        int contrastColorBackground = ContrastColor(backgroundColor);
        toolbar.setBackgroundColor(backgroundColor);
        toolbar.setTitleTextColor(contrastColorBackground);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(contrastColorBackground == Color.BLACK ? toolbarIcons[0] : toolbarIcons[1]);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(view -> runnable.run());
    }

    public static void setUpToolbar(Toolbar toolbar, String title, int[] toolbarIcons, int backgroundColor, AppCompatActivity activity, Runnable runnable, boolean noBackground) {
        if (toolbar == null || activity == null || toolbarIcons == null || toolbarIcons.length < 2) {
            return;
        }
        toolbar.setTitle(title);
        int contrastColorBackground = ContrastColor(backgroundColor);
        if (!noBackground) {
            toolbar.setBackgroundColor(backgroundColor);
        }
        toolbar.setTitleTextColor(contrastColorBackground);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(contrastColorBackground == Color.BLACK ? toolbarIcons[0] : toolbarIcons[1]);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(view -> runnable.run());
    }
}
