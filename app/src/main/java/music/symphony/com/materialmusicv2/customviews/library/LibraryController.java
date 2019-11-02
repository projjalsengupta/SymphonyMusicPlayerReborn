package music.symphony.com.materialmusicv2.customviews.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.customviews.others.CircleProgressBar;
import music.symphony.com.materialmusicv2.utils.listeners.OnSwipeTouchListener;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.getRoundedSmallDrawable;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils.getPlayPauseResourceWhite;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils.getRepeatResourceWhite;

public class LibraryController extends LinearLayout implements View.OnClickListener {

    private LinearLayout controlsContainer;
    private CircleProgressBar progressBar;
    public ImageView albumArt;
    private TextView songName;
    private ImageButton shuffle;
    private ImageButton playPrevious;
    private ImageButton playPause;
    private ImageButton playNext;
    private ImageButton repeat;

    private ObjectAnimator rotation;

    private boolean wasPlaying = false;
    private boolean hasRotationStarted = false;

    private OnClickEventDetectedListener onClickEventDetectedListener = null;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public LibraryController(Context context) {
        super(context);
        init();
    }

    public LibraryController(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LibraryController(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), SymphonyApplication.getInstance().getPreferenceUtils().getMainScreenStyle() ? R.layout.library_controller_two : R.layout.library_controller_one, this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        controlsContainer = findViewById(R.id.controlsContainer);
        progressBar = findViewById(R.id.progressBar);
        albumArt = findViewById(R.id.albumArt);
        songName = findViewById(R.id.songName);
        shuffle = findViewById(R.id.shuffle);
        playPrevious = findViewById(R.id.playPrevious);
        playPause = findViewById(R.id.playPause);
        playNext = findViewById(R.id.playNext);
        repeat = findViewById(R.id.repeat);

        rotation = ObjectAnimator.ofFloat(albumArt, "rotation", 0, 359);
        rotation.setDuration(5000);
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.setRepeatMode(ObjectAnimator.RESTART);
        rotation.setInterpolator(new LinearInterpolator());

        rotation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                animation.setInterpolator(new LinearInterpolator());
            }
        });

        if (shuffle != null) {
            shuffle.setOnClickListener(this);
        }
        if (playPrevious != null) {
            playPrevious.setOnClickListener(this);
        }
        playPause.setOnClickListener(this);
        if (playNext != null) {
            playNext.setOnClickListener(this);
        }
        if (repeat != null) {
            repeat.setOnClickListener(this);
        }

        if (songName != null && SymphonyApplication.getInstance().getPreferenceUtils().getMainScreenStyle()) {
            songName.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
                @Override
                public void onClick() {
                    super.onClick();
                    if (onClickEventDetectedListener != null) {
                        onClickEventDetectedListener.onBodyClicked();
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
        }

        int accentColor = ThemeUtils.getThemeAccentColor(getContext());
        setColors(accentColor, ContrastColor(accentColor));
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            if (wasPlaying && rotation != null) {
                rotation.resume();
            }
        } else {
            if (wasPlaying && rotation != null) {
                rotation.pause();
            }
        }
    }

    public void setSongName(String name) {
        if (songName != null) {
            songName.setText(name);
        }
    }

    public void setShuffle(boolean state) {
        if (shuffle != null) {
            if (state) {
                shuffle.setImageAlpha(255);
            } else {
                shuffle.setImageAlpha(82);
            }
        }
    }

    public void setRepeat(int state) {
        if (repeat != null) {
            repeat.setImageResource(getRepeatResourceWhite(state));
            if (state != 0) {
                repeat.setImageAlpha(255);
            } else {
                repeat.setImageAlpha(82);
            }
        }
    }

    public void setPlayPause(boolean isPlaying) {
        if (wasPlaying != isPlaying) {
            if (playPause != null) {
                playPause.setImageResource(getPlayPauseResourceWhite(isPlaying));
            }
            if (rotation != null) {
                if (isPlaying) {
                    if (!hasRotationStarted) {
                        rotation.start();
                        hasRotationStarted = true;
                    }
                    rotation.resume();
                } else {
                    rotation.pause();
                }
            }
            wasPlaying = isPlaying;
        }
    }

    public void setOnClickEventDetectedListener(OnClickEventDetectedListener onClickEventDetectedListener) {
        this.onClickEventDetectedListener = onClickEventDetectedListener;
    }

    @Override
    public void onClick(View v) {
        if (onClickEventDetectedListener != null) {
            switch (v.getId()) {
                case R.id.shuffle: {
                    onClickEventDetectedListener.onShuffleClicked();
                    break;
                }
                case R.id.playPrevious: {
                    onClickEventDetectedListener.onPlayPreviousClicked();
                    break;
                }
                case R.id.playPause: {
                    onClickEventDetectedListener.onPlayPauseClicked();
                    break;
                }
                case R.id.playNext: {
                    onClickEventDetectedListener.onPlayNextClicked();
                    break;
                }
                case R.id.repeat: {
                    onClickEventDetectedListener.onRepeatClicked();
                    break;
                }
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        songName = null;
        shuffle = null;
        playPrevious = null;
        playPause = null;
        playNext = null;
        repeat = null;

        onClickEventDetectedListener = null;

        setOnClickListener(null);

        if (rotation != null) {
            rotation.cancel();
            rotation.removeAllListeners();
            rotation = null;
        }

        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    public void setPlaybackPosition(int playbackPosition, int duration) {
        if (progressBar != null) {
            progressBar.setProgress(((float) playbackPosition / (float) duration) * 100);
        }
    }

    public void onBitmapChanged(Bitmap bitmap) {
        if (executorService != null) {
            executorService.execute(() -> {
                final Drawable roundedAlbumArt = getRoundedSmallDrawable(getContext(), bitmap);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (albumArt != null) {
                        albumArt.setImageDrawable(roundedAlbumArt);
                    }
                });
            });
        }
    }

    private void setColors(int backgroundColor, int foregroundColor) {
        if (controlsContainer != null) {
            controlsContainer.setBackgroundColor(backgroundColor);
        }
        if (progressBar != null) {
            progressBar.setColor(foregroundColor);
        }
        setTextColor(foregroundColor, songName);
        setColorFilter(foregroundColor, shuffle, playPrevious, playPause, playNext, repeat);
    }

    public interface OnClickEventDetectedListener {
        void onShuffleClicked();

        void onPlayPreviousClicked();

        void onPlayPauseClicked();

        void onPlayNextClicked();

        void onRepeatClicked();

        void onBodyClicked();
    }
}
