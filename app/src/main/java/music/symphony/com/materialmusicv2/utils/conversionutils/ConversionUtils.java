package music.symphony.com.materialmusicv2.utils.conversionutils;

public class ConversionUtils {
    public static String covertMilisToTimeString(long milis) {
        StringBuilder stringBuilder = new StringBuilder();
        milis -= (milis % 1000) / 1000;
        long secs = milis / 1000;
        long mins = secs / 60;
        final long finalSec = secs % 60;
        final long finalMin = mins % 60;
        final long finalHour = mins / 60;
        if (finalHour == 0) {
            stringBuilder.append(finalMin);
            if (finalSec < 10) {
                stringBuilder.append(":0");
            } else {
                stringBuilder.append(":");
            }
            stringBuilder.append(finalSec);
        } else {
            stringBuilder.append(finalHour);
            if (finalMin < 10) {
                stringBuilder.append(":0");
            } else {
                stringBuilder.append(":");
            }
            stringBuilder.append(finalMin);
            if (finalSec < 10) {
                stringBuilder.append(":0");
            } else {
                stringBuilder.append(":");
            }
            stringBuilder.append(finalSec);
        }
        return stringBuilder.toString();
    }

    public static String covertMilisToTimeString(int milis) {
        StringBuilder stringBuilder = new StringBuilder();
        milis -= (milis % 1000) / 1000;
        int secs = milis / 1000;
        int mins = secs / 60;
        final int finalSec = secs % 60;
        final int finalMin = mins % 60;
        final int finalHour = mins / 60;
        if (finalHour == 0) {
            stringBuilder.append(finalMin);
            if (finalSec < 10) {
                stringBuilder.append(":0");
            } else {
                stringBuilder.append(":");
            }
            stringBuilder.append(finalSec);
        } else {
            stringBuilder.append(finalHour);
            if (finalMin < 10) {
                stringBuilder.append(":0");
            } else {
                stringBuilder.append(":");
            }
            stringBuilder.append(finalMin);
            if (finalSec < 10) {
                stringBuilder.append(":0");
            } else {
                stringBuilder.append(":");
            }
            stringBuilder.append(finalSec);
        }
        return stringBuilder.toString();
    }
}
