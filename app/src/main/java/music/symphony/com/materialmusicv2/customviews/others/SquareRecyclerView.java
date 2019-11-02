package music.symphony.com.materialmusicv2.customviews.others;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.RecyclerView;

public class SquareRecyclerView extends RecyclerView {
    public SquareRecyclerView(Context context) {
        super(context);
    }

    public SquareRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
