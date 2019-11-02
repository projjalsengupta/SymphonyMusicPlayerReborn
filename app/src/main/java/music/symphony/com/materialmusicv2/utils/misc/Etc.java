package music.symphony.com.materialmusicv2.utils.misc;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.File;

import es.dmoral.toasty.Toasty;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;

public class Etc {

    public static String getRealString(String string) {
        try {
            if (string != null) {
                string = string.contains("(") ? string.substring(0, string.indexOf("(")) : string;
                string = string.contains("[") ? string.substring(0, string.indexOf("[")) : string;
                string = string.contains("-") ? string.substring(0, string.indexOf("-")) : string;
                string = string.contains("<") ? string.substring(0, string.indexOf("<")) : string;
                string = string.contains(";") ? string.substring(0, string.indexOf(";")) : string;
                string = string.contains("&") ? string.substring(0, string.indexOf("&")) : string;
                string = string.contains(",") ? string.substring(0, string.indexOf(",")) : string;
                string = string.contains("/") ? string.substring(0, string.indexOf("/")) : string;
                string = string.trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static void setAudioAsRingtone(File ringtoneFile, Context context) {
        if (ringtoneFile == null || context == null) {
            return;
        }

        boolean canWriteSettings;
        canWriteSettings = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M || Settings.System.canWrite(context);

        if (!canWriteSettings) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                postToast(R.string.grant_write_permission, context, TOAST_NORMAL);
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                context.startActivity(intent);
            } else {
                postToast(R.string.error_label, context, TOAST_ERROR);
            }
        } else {
            try {
                ContentValues content = new ContentValues();
                content.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
                content.put(MediaStore.MediaColumns.TITLE, ringtoneFile.getName());
                content.put(MediaStore.MediaColumns.SIZE, ringtoneFile.length());
                content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
                content.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                content.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                content.put(MediaStore.Audio.Media.IS_ALARM, false);
                content.put(MediaStore.Audio.Media.IS_MUSIC, false);

                Uri uri = MediaStore.Audio.Media.getContentUriForPath(
                        ringtoneFile.getAbsolutePath());

                if (uri != null) {
                    context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"",
                            null);
                }
                Uri newUri = null;
                if (uri != null) {
                    newUri = context.getContentResolver().insert(uri, content);
                }
                RingtoneManager.setActualDefaultRingtoneUri(
                        context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE,
                        newUri);

                postToast(R.string.ringtone_set, context, TOAST_SUCCESS);
            } catch (Exception e) {
                e.printStackTrace();
                postToast(R.string.error_label, context, TOAST_ERROR);
            }
        }
    }

    private static boolean isIntentResolved(Context context, Intent intent) {
        return (intent != null && context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null);
    }

    public static boolean isMIUI(Context context) {
        return isIntentResolved(context, new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT))
                || isIntentResolved(context, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
                || isIntentResolved(context, new Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST").addCategory(Intent.CATEGORY_DEFAULT))
                || isIntentResolved(context, new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"))
        );
    }

    public static boolean isConnectedToWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) SymphonyApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo != null) {
                return networkInfo.isConnected();
            }
        }
        return false;
    }

    public static float dipToPixels(Context context, float dipValue) {
        if (context == null) {
            return 0;
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static final int TOAST_NORMAL = 0;
    public static final int TOAST_SUCCESS = 1;
    public static final int TOAST_ERROR = 2;
    public static final int TOAST_INFO = 3;

    public static void postToast(int stringID, Context applicationContext, int toastType) {
        if (applicationContext == null) {
            return;
        }
        switch (toastType) {
            case 0: {
                Toasty.normal(applicationContext, stringID).show();
                break;
            }
            case 1: {
                Toasty.success(applicationContext, stringID).show();
                break;
            }
            case 2: {
                Toasty.error(applicationContext, stringID).show();
                break;
            }
            case 3: {
                Toasty.info(applicationContext, stringID).show();
                break;
            }
        }
    }

    public static void postToast(String string, Context applicationContext, int toastType) {
        if (applicationContext == null) {
            return;
        }
        switch (toastType) {
            case 0: {
                Toasty.normal(applicationContext, string).show();
                break;
            }
            case 1: {
                Toasty.success(applicationContext, string).show();
                break;
            }
            case 2: {
                Toasty.error(applicationContext, string).show();
                break;
            }
            case 3: {
                Toasty.info(applicationContext, string).show();
                break;
            }
        }
    }
}