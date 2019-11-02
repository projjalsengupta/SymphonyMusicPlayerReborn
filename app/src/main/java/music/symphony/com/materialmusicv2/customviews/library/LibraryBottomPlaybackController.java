package music.symphony.com.materialmusicv2.customviews.library;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.symphony.com.materialmusicv2.objects.events.PlaybackPosition;
import music.symphony.com.materialmusicv2.objects.events.PlaybackState;
import music.symphony.com.materialmusicv2.utils.listeners.OnSwipeTouchListener;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils.getPlayPauseResourceBlack;

public class LibraryBottomPlaybackController extends LinearLayout implements View.OnClickListener {

    private MaterialProgressBar songProgress;
    private ImageView up;
    private TextView songName;
    private ImageButton playPause;
    private ImageButton openPlayingQueue;

    private OnClickEventDetectedListener onClickEventDetectedListener = null;

    public LibraryBottomPlaybackController(Context context) {
        super(context);
        init();
    }

    public LibraryBottomPlaybackController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LibraryBottomPlaybackController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.library_bottom_controller, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        songProgress = findViewById(R.id.songProgress);
        up = findViewById(R.id.up);
        songName = findViewById(R.id.songName);
        playPause = findViewById(R.id.playPause);
        openPlayingQueue = findViewById(R.id.openPlayingQueue);

        songName.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onClick() {
                super.onClick();
                if (onClickEventDetectedListener != null) {
                    onClickEventDetectedListener.onSongNameClicked();
                }
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                if (onClickEventDetectedListener != null) {
                    onClickEventDetectedListener.onPlayNextClicked();
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                if (onClickEventDetectedListener != null) {
                    onClickEventDetectedListener.onPlayPreviousClicked();
                }
            }
        });

        playPause.setOnClickListener(this);
        openPlayingQueue.setOnClickListener(this);

        setOnClickListener(view -> {
            if (onClickEventDetectedListener != null) {
                onClickEventDetectedListener.onSongNameClicked();
            }
        });
    }

    private void setSongProgress(int progress, int maxProgress) {
        if (songProgress != null) {
            if (songProgress.getMax() != maxProgress) {
                songProgress.setMax(maxProgress);
            }
            songProgress.setProgress(progress);
        }
    }

    public void setSongName(String name) {
        if (songName != null) {
            songName.setText(name);
        }
    }

    private void setPlayPause(boolean isPlaying) {
        if (playPause != null) {
            playPause.setImageResource(getPlayPauseResourceBlack(isPlaying));
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        int contrastColor = ContrastColor(color);
        setColorFilter(contrastColor, songProgress);
        setColorFilter(contrastColor, up);
        setTextColor(contrastColor, songName);
        setColorFilter(contrastColor, playPause, openPlayingQueue);
    }

    public void setOnClickEventDetectedListener(OnClickEventDetectedListener onClickEventDetectedListener) {
        this.onClickEventDetectedListener = onClickEventDetectedListener;
    }

    @Override
    public void onClick(View v) {
        if (onClickEventDetectedListener != null) {
            if (v.getId() == R.id.playPause) {
                onClickEventDetectedListener.onPlayPauseClicked();
            } else if (v.getId() == R.id.openPlayingQueue) {
                onClickEventDetectedListener.onOpenPlayingQueueClicked();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackState playbackState) {
        setPlayPause(playbackState.state);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackPosition playbackPosition) {
        setSongProgress(playbackPosition.position, playbackPosition.duration);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong) {
        setSongName(currentPlayingSong.songName);
    }

    public interface OnClickEventDetectedListener {

        void onPlayPreviousClicked();

        void onPlayPauseClicked();

        void onPlayNextClicked();

        void onSongNameClicked();

        void onOpenPlayingQueueClicked();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        } else {
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        songProgress = null;
        songName = null;
        playPause = null;
        openPlayingQueue = null;

        onClickEventDetectedListener = null;

        setOnClickListener(null);
    }

    public void setProgressColor(int progressColor, int secondaryProgressColor) {
        setColorFilter(progressColor, secondaryProgressColor, songProgress);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }
}
