package music.symphony.com.materialmusicv2.customviews.nowplaying;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.adapters.NowPlayingBackgroundAdapter;
import music.symphony.com.materialmusicv2.objects.Song;

public class NowPlayingBackground extends RelativeLayout {

    private int previousPosition = -1;

    private ViewPager backgroundViewPager;

    private ScrollListener scrollListener;

    public NowPlayingBackground(Context context) {
        super(context);
        init();
    }

    public NowPlayingBackground(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NowPlayingBackground(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.now_playing_background, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        backgroundViewPager = findViewById(R.id.nowPlayingBackgroundContainer);
        setUpBackgroundViewPager();
    }

    public void scrollTo(int position) {
        if (backgroundViewPager != null && backgroundViewPager.getCurrentItem() != position) {
            backgroundViewPager.setCurrentItem(position);
        }
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (backgroundViewPager != null) {
            backgroundViewPager.setAdapter(null);
            backgroundViewPager = null;
        }
    }

    private void setUpBackgroundViewPager() {
        backgroundViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if (previousPosition == i) {
                    return;
                }
                if (scrollListener != null) {
                    scrollListener.onBackgroundScrolled(i);
                }
                previousPosition = i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
    }

    public void setSongs(ArrayList<Song> songs) {
        NowPlayingBackgroundAdapter backgroundAdapter = new NowPlayingBackgroundAdapter(getContext(), songs);
        backgroundViewPager.setAdapter(backgroundAdapter);
    }

    public void setPreviousPosition(int previousPosition) {
        this.previousPosition = previousPosition;
    }

    public interface ScrollListener {
        void onBackgroundScrolled(int position);
    }
}
