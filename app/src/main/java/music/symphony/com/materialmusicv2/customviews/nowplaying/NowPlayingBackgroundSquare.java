package music.symphony.com.materialmusicv2.customviews.nowplaying;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class NowPlayingBackgroundSquare extends NowPlayingBackground {

    public NowPlayingBackgroundSquare(Context context) {
        super(context);
    }

    public NowPlayingBackgroundSquare(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NowPlayingBackgroundSquare(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
