package music.symphony.com.materialmusicv2.utils.themeutils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;

public class ThemeUtils {

    public static ArrayList<Integer> getAllAccentColors() {
        ArrayList<Integer> accentColors = new ArrayList<>();
        //Red
        accentColors.add(Color.parseColor("#ff8a80"));
        accentColors.add(Color.parseColor("#ff5252"));
        accentColors.add(Color.parseColor("#ff1744"));
        accentColors.add(Color.parseColor("#d50000"));
        //Pink
        accentColors.add(Color.parseColor("#ff80ab"));
        accentColors.add(Color.parseColor("#ff4081"));
        accentColors.add(Color.parseColor("#f50057"));
        accentColors.add(Color.parseColor("#c51162"));
        //Purple
        accentColors.add(Color.parseColor("#ea80fc"));
        accentColors.add(Color.parseColor("#e040fb"));
        accentColors.add(Color.parseColor("#d500f9"));
        accentColors.add(Color.parseColor("#aa00ff"));
        //Deep Purple
        accentColors.add(Color.parseColor("#b388ff"));
        accentColors.add(Color.parseColor("#7c4dff"));
        accentColors.add(Color.parseColor("#651fff"));
        accentColors.add(Color.parseColor("#6200ea"));
        //Indigo
        accentColors.add(Color.parseColor("#8c9eff"));
        accentColors.add(Color.parseColor("#536dfe"));
        accentColors.add(Color.parseColor("#3d5afe"));
        accentColors.add(Color.parseColor("#304ffe"));
        //Blue
        accentColors.add(Color.parseColor("#82b1ff"));
        accentColors.add(Color.parseColor("#448aff"));
        accentColors.add(Color.parseColor("#2979ff"));
        accentColors.add(Color.parseColor("#2962ff"));
        //Light Blue
        accentColors.add(Color.parseColor("#80d8ff"));
        accentColors.add(Color.parseColor("#40c4ff"));
        accentColors.add(Color.parseColor("#00b0ff"));
        accentColors.add(Color.parseColor("#0091ea"));
        //Cyan
        accentColors.add(Color.parseColor("#84ffff"));
        accentColors.add(Color.parseColor("#18ffff"));
        accentColors.add(Color.parseColor("#00e5ff"));
        accentColors.add(Color.parseColor("#00b8d4"));
        //Teal
        accentColors.add(Color.parseColor("#a7ffeb"));
        accentColors.add(Color.parseColor("#64ffda"));
        accentColors.add(Color.parseColor("#1de9b6"));
        accentColors.add(Color.parseColor("#00bfa5"));
        //Green
        accentColors.add(Color.parseColor("#b9f6ca"));
        accentColors.add(Color.parseColor("#69f0ae"));
        accentColors.add(Color.parseColor("#00e676"));
        accentColors.add(Color.parseColor("#00c853"));
        //Light Green
        accentColors.add(Color.parseColor("#ccff90"));
        accentColors.add(Color.parseColor("#b2ff59"));
        accentColors.add(Color.parseColor("#76ff03"));
        accentColors.add(Color.parseColor("#64dd17"));
        //Lime
        accentColors.add(Color.parseColor("#f4ff81"));
        accentColors.add(Color.parseColor("#eeff41"));
        accentColors.add(Color.parseColor("#c6ff00"));
        accentColors.add(Color.parseColor("#aeea00"));
        //Yellow
        accentColors.add(Color.parseColor("#ffff8d"));
        accentColors.add(Color.parseColor("#ffff00"));
        accentColors.add(Color.parseColor("#ffea00"));
        accentColors.add(Color.parseColor("#ffd600"));
        //Amber
        accentColors.add(Color.parseColor("#ffe57f"));
        accentColors.add(Color.parseColor("#ffd740"));
        accentColors.add(Color.parseColor("#ffc400"));
        accentColors.add(Color.parseColor("#ffab00"));
        //Orange
        accentColors.add(Color.parseColor("#ffd180"));
        accentColors.add(Color.parseColor("#ffab40"));
        accentColors.add(Color.parseColor("#ff9100"));
        accentColors.add(Color.parseColor("#ff6d00"));
        //Deep Orange
        accentColors.add(Color.parseColor("#ff9e80"));
        accentColors.add(Color.parseColor("#ff6e40"));
        accentColors.add(Color.parseColor("#ff3d00"));
        accentColors.add(Color.parseColor("#dd2c00"));

        return accentColors;
    }

    public static boolean isThemeDarkOrBlack() {
        try {
            return SymphonyApplication.getInstance().getPreferenceUtils().getTheme() != 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static int getThemeAccentColor(final Context context) {
        try {
            if (context != null) {
                TypedValue typedValue = new TypedValue();
                TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
                int color = a.getColor(0, 0);
                a.recycle();
                return color;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Color.GRAY;
    }

    public static int getThemeTextColorPrimary(final Context context) {
        try {
            if (context != null) {
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                TypedArray arr =
                        context.obtainStyledAttributes(typedValue.data, new int[]{
                                android.R.attr.textColorPrimary});
                int color = arr.getColor(0, -1);
                arr.recycle();
                return color;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Color.WHITE;
    }

    public static int getThemePrimaryColor(final Context context) {
        try {
            if (context != null) {
                TypedValue typedValue = new TypedValue();
                TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
                int color = a.getColor(0, 0);
                a.recycle();
                return color;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Color.GRAY;
    }

    public static int getThemeWindowBackgroundColor(final Context context) {
        try {
            if (context != null) {
                TypedValue typedValue = new TypedValue();
                TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.windowBackground});
                int color = a.getColor(0, 0);
                a.recycle();
                return color;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Color.BLACK;
    }

    public static int getThemeColorControlHighlight(final Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                if (context != null) {
                    TypedValue typedValue = new TypedValue();
                    TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.colorControlHighlight});
                    int color = a.getColor(0, 0);
                    a.recycle();
                    return color;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return getThemeWindowBackgroundColor(context);
        }
        return Color.BLACK;
    }

    public static int getBottomSheetDialogFragmentTheme(Context context) {
        if (context == null) {
            return R.style.BottomSheetDialogTheme_Light;
        }
        int theme = SymphonyApplication.getInstance().getPreferenceUtils().getTheme();
        switch (theme % 3) {
            case 0: {
                return R.style.BottomSheetDialogTheme_Light;
            }
            case 1: {
                return R.style.BottomSheetDialogTheme_Dark;
            }
            case 2: {
                return R.style.BottomSheetDialogTheme_Black;
            }
            default: {
                return R.style.BottomSheetDialogTheme_Light;
            }
        }
    }

    public static int getTheme(Context context) {
        if (context == null) {
            return R.style.AppTheme_NoActionBar_Light_Theme35;
        }
        int theme = SymphonyApplication.getInstance().getPreferenceUtils().getTheme();
        int accentColor = SymphonyApplication.getInstance().getPreferenceUtils().getAccentColor() + 1;
        try {
            String fieldName = "AppTheme_NoActionBar_" + ((theme == 0) ? "Light" : ((theme == 1)) ? "Dark" : "Black") + "_Theme" + accentColor;
            Object obj = R.style.class.getField(fieldName).get(R.style.class);
            if (obj instanceof Integer) {
                return (int) obj;
            } else {
                return R.style.AppTheme_NoActionBar_Light_Theme35;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.style.AppTheme_NoActionBar_Light_Theme35;
        }
    }

    public static void setDarkStatusBarIcons(Activity activity, boolean dark) {
        if (activity == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = activity.getWindow().getDecorView();
            if (dark) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decor.setSystemUiVisibility(0);
            }
        }
    }
}
