package music.symphony.com.materialmusicv2.customviews.nowplaying;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;

import music.symphony.com.materialmusicv2.R;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setBackgroundColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;

public class NowPlayingLyrics extends LinearLayout implements View.OnClickListener {

    private NestedScrollView scrollView;
    private TextView lyrics;
    private LinearLayout noLyricsOptionsContainer;
    private MaterialButton search;
    private MaterialButton add;

    private ClickEventListener clickEventListener = null;

    public NowPlayingLyrics(Context context) {
        super(context);
        init();
    }

    public NowPlayingLyrics(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NowPlayingLyrics(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.now_playing_lyrics, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        scrollView = findViewById(R.id.scrollView);
        lyrics = findViewById(R.id.lyrics);
        noLyricsOptionsContainer = findViewById(R.id.noLyricsOptionsContainer);
        search = findViewById(R.id.search);
        add = findViewById(R.id.add);
        search.setOnClickListener(this);
        add.setOnClickListener(this);
    }

    public void setColors(int tintColor, int backgroundColor) {
        setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_top_rect_transparent));
        setBackgroundColorFilter(backgroundColor, this);
        setTextColor(tintColor, lyrics);
        if (search != null) {
            search.setBackgroundColor(tintColor);
            search.setTextColor(backgroundColor);
            search.setIconTint(ColorStateList.valueOf(backgroundColor));
        }
        if (add != null) {
            add.setBackgroundColor(tintColor);
            add.setTextColor(backgroundColor);
            add.setIconTint(ColorStateList.valueOf(backgroundColor));
        }
    }

    public void setLyrics(String lyrics) {
        if (lyrics != null && this.lyrics != null) {
            this.scrollView.scrollTo(0, 0);
            if (noLyricsOptionsContainer != null) {
                if (lyrics.equals(getContext().getString(R.string.no_lyrics))) {
                    noLyricsOptionsContainer.setVisibility(View.VISIBLE);
                    this.lyrics.setText(null);
                } else {
                    this.lyrics.setText(lyrics);
                    noLyricsOptionsContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    public void setClickEventListener(ClickEventListener clickEventListener) {
        this.clickEventListener = clickEventListener;
    }

    @Override
    public void onClick(View v) {
        if (clickEventListener != null) {
            switch (v.getId()) {
                case R.id.search: {
                    clickEventListener.onSearchClicked();
                    break;
                }
                case R.id.add: {
                    clickEventListener.onAddClicked();
                    break;
                }
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        scrollView = null;
        lyrics = null;
        noLyricsOptionsContainer = null;
        search = null;
        add = null;

        clickEventListener = null;
    }

    public interface ClickEventListener {
        void onSearchClicked();

        void onAddClicked();
    }
}
