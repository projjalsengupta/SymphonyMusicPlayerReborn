package music.symphony.com.materialmusicv2.utils.bitmaputils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.palette.graphics.Palette;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.compareColors;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.darken;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.lighten;

public class BitmapUtils {

    private static final String TAG = "bitmaputils";

    public static Bitmap decodeResource(Resources res, int id, int reqWidth, int reqHeight, Bitmap.Config config) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPreferredConfig = config;
            BitmapFactory.decodeResource(res, id, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inDensity = options.outWidth;
            options.inTargetDensity = reqWidth * options.inSampleSize;
            options.inMutable = true;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeResource(res, id, options);
        } catch (Exception e) {
            Log.v(TAG, "error while decoding resource");
            return null;
        }
    }

    public static byte[] decodeUrl(String link) {
        try {
            URL url = new URL(link);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return getBytes(input);
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getBytes(InputStream inputStream) throws IOException {
        byte[] bytesResult;
        try (ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            bytesResult = byteBuffer.toByteArray();
        }
        return bytesResult;
    }

    public static Bitmap decodeByteArray(byte[] data, int length, int reqWidth, int reqHeight, Bitmap.Config config) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPreferredConfig = config;
            BitmapFactory.decodeByteArray(data, 0, length, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inDensity = options.outWidth;
            options.inTargetDensity = reqWidth * options.inSampleSize;
            options.inMutable = true;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(data, 0, length, options);
        } catch (Exception e) {
            Log.v(TAG, "error while decoding byte array");
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    @NonNull
    public static int[] getDominantColors(Bitmap bitmap) {
        if (bitmap == null) {
            return new int[]{Color.BLACK, Color.WHITE, Color.WHITE};
        }
        try {
            if (!bitmap.isRecycled()) {
                Palette palette = Palette.from(bitmap).resizeBitmapArea(5000).generate();
                Palette.Swatch backgroundSwatch;
                Palette.Swatch textColorSwatch;
                backgroundSwatch = palette.getDominantSwatch();
                int backgroundColor = Color.BLACK;
                if (backgroundSwatch != null) {
                    backgroundColor = backgroundSwatch.getRgb();
                }
                int backgroundContrastColor = ContrastColor(backgroundColor);
                if (backgroundContrastColor == Color.BLACK) {
                    textColorSwatch = palette.getDarkVibrantSwatch();
                    if (textColorSwatch == null) {
                        textColorSwatch = palette.getDarkMutedSwatch();
                    }
                } else {
                    textColorSwatch = palette.getLightVibrantSwatch();
                    if (textColorSwatch == null) {
                        textColorSwatch = palette.getLightMutedSwatch();
                    }
                }
                int textColor = backgroundContrastColor;
                if (textColorSwatch != null) {
                    textColor = textColorSwatch.getRgb();
                    textColor = lightenOrDarkenColorUntilTheyAreContrasty(backgroundColor, textColor);
                }
                return new int[]{backgroundColor, textColor, textColor};
            } else {
                return new int[]{Color.BLACK, Color.WHITE, Color.WHITE};
            }
        } catch (Exception e) {
            return new int[]{Color.BLACK, Color.WHITE, Color.WHITE};
        }
    }

    public static int lightenOrDarkenColorUntilTheyAreContrasty(int backgroundColor, int textColor) {
        double comparison = compareColors(backgroundColor, textColor);
        int backgroundContrastColor = ContrastColor(backgroundColor);
        int counter = 10;
        while (comparison < 0.5 && counter > 0) {
            if (backgroundContrastColor == Color.BLACK) {
                textColor = darken(textColor, 1 - comparison);
            } else {
                textColor = lighten(textColor, 1 - comparison);
            }
            comparison = compareColors(backgroundColor, textColor);
            counter--;
        }
        return textColor;
    }

    public static Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        if (image == null) {
            return null;
        }
        try {
            if (!image.isRecycled()) {
                if (maxHeight > 0 && maxWidth > 0) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    float ratioBitmap = (float) width / (float) height;
                    float ratioMax = (float) maxWidth / (float) maxHeight;
                    int finalWidth = maxWidth;
                    int finalHeight = maxHeight;
                    if (ratioMax > 1) {
                        finalWidth = (int) ((float) maxHeight * ratioBitmap);
                    } else {
                        finalHeight = (int) ((float) maxWidth / ratioBitmap);
                    }
                    image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
                    return image;
                } else {
                    return image;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static RoundedBitmapDrawable getRoundedSmallDrawable(Context context, Bitmap bitmap) {
        try {
            int fiftyDp = (int) (50 * context.getResources().getDisplayMetrics().density);
            bitmap = Bitmap.createScaledBitmap(bitmap, fiftyDp, fiftyDp, false);
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            return roundedBitmapDrawable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static RoundedBitmapDrawable getRoundedDrawable(Context context, Bitmap bitmap) {
        try {
            RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
            roundedBitmapDrawable.setCircular(true);
            return roundedBitmapDrawable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int cornerDps, Context context, boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int cornerSizePx = (int) (context.getResources().getDisplayMetrics().density) * cornerDps;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        if (squareTL) {
            canvas.drawRect(0, 0, (float) (bitmap.getWidth() / 2.0), (float) (bitmap.getHeight() / 2.0), paint);
        }
        if (squareTR) {
            canvas.drawRect((float) (bitmap.getWidth() / 2.0), 0, bitmap.getWidth(), (float) (bitmap.getHeight() / 2.0), paint);
        }
        if (squareBL) {
            canvas.drawRect(0, (float) (bitmap.getHeight() / 2.0), (float) (bitmap.getWidth() / 2.0), bitmap.getHeight(), paint);
        }
        if (squareBR) {
            canvas.drawRect((float) (bitmap.getWidth() / 2.0), (float) (bitmap.getHeight() / 2.0), bitmap.getWidth(), bitmap.getHeight(), paint);
        }

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap drawBitmapBackground(Bitmap bitmap, int backgroundColor) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(backgroundColor);

        // draw bitmap
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
