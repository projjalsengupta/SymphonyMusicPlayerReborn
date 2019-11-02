
package music.symphony.com.materialmusicv2.customviews.others;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import music.symphony.com.materialmusicv2.R;

public class RoundedSquareImageView extends SquareImageView {

    public static final int CORNER_NONE = 0;
    public static final int CORNER_TOP_LEFT = 1;
    public static final int CORNER_TOP_RIGHT = 2;
    public static final int CORNER_BOTTOM_RIGHT = 4;
    public static final int CORNER_BOTTOM_LEFT = 8;
    public static final int CORNER_ALL = 15;

    private static final int[] CORNERS = {CORNER_TOP_LEFT,
            CORNER_TOP_RIGHT,
            CORNER_BOTTOM_RIGHT,
            CORNER_BOTTOM_LEFT};

    private final Path path = new Path();
    private int cornerRadius;
    private int roundedCorners;

    public RoundedSquareImageView(Context context) {
        this(context, null);
    }

    public RoundedSquareImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedSquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedSquareImageView);
        cornerRadius = a.getDimensionPixelSize(R.styleable.RoundedSquareImageView_cornerRadius, 0);
        roundedCorners = a.getInt(R.styleable.RoundedSquareImageView_roundedCorners, CORNER_NONE);
        a.recycle();
    }

    public void setCornerRadius(int radius) {
        if (cornerRadius != radius) {
            cornerRadius = radius;
            setPath();
            invalidate();
        }
    }

    public void setRoundedCorners(int corners) {
        if (roundedCorners != corners) {
            roundedCorners = corners;
            setPath();
            invalidate();
        }
    }

    public boolean isCornerRounded(int corner) {
        return (roundedCorners & corner) == corner;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!path.isEmpty()) {
            canvas.clipPath(path);
        }

        super.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setPath();
    }

    private void setPath() {
        path.rewind();

        if (cornerRadius >= 1f && roundedCorners != CORNER_NONE) {
            final float[] radii = new float[8];

            for (int i = 0; i < 4; i++) {
                if (isCornerRounded(CORNERS[i])) {
                    radii[2 * i] = cornerRadius;
                    radii[2 * i + 1] = cornerRadius;
                }
            }

            path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()),
                    radii, Path.Direction.CW);
        }
    }
}