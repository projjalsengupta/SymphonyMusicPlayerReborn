package music.symphony.com.materialmusicv2.utils.gaplessfadingmediaplayer.fadingmediaplayer;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import music.symphony.com.materialmusicv2.SymphonyApplication;

public class FadingMediaPlayer extends MediaPlayer implements FadeMethods {

    private static final String TAG = "fadingmediaplayer";

    private boolean isPlaying;

    private float highVolume = 1.0f;
    private float deltaVolume = 0.1f;
    private float currentVolume = 1.0f;

    private long fadeDuration = 550;
    private long interval = 50;

    private CountDownTimer pauseTimer;
    private CountDownTimer resumeTimer;

    public FadingMediaPlayer() {
        super();
        new Handler(Looper.getMainLooper()).post(() -> {
            pauseTimer = new CountDownTimer(fadeDuration, interval) {
                @Override
                public void onTick(long millisUntilFinished) {
                    currentVolume -= deltaVolume;
                    if (currentVolume > 1.0f) {
                        currentVolume = 1.0f;
                    }
                    if (currentVolume < 0.0f) {
                        currentVolume = 0.0f;
                    }
                    try {
                        setVolume(currentVolume, currentVolume);
                    } catch (IllegalStateException e) {
                        Log.v(TAG, "Illegal state");
                    }
                }

                @Override
                public void onFinish() {
                    try {
                        FadingMediaPlayer.super.pause();
                    } catch (IllegalStateException e) {
                        Log.v(TAG, "Illegal state");
                    }
                }
            };
            resumeTimer = new CountDownTimer(fadeDuration, interval) {
                @Override
                public void onTick(long millisUntilFinished) {
                    currentVolume += deltaVolume;
                    if (currentVolume < 0.0f) {
                        currentVolume = 0.0f;
                    }
                    if (currentVolume > 1.0f) {
                        currentVolume = 1.0f;
                    }
                    try {
                        setVolume(currentVolume, currentVolume);
                    } catch (IllegalStateException e) {
                        Log.v(TAG, "Illegal state");
                    }
                }

                @Override
                public void onFinish() {
                }
            };
        });
    }

    @Override
    public void pause() {
        try {
            if (!SymphonyApplication.getInstance().getPreferenceUtils().getFade()) {
                super.pause();
            } else {
                isPlaying = false;
                if (resumeTimer != null) {
                    resumeTimer.cancel();
                }
                if (pauseTimer != null) {
                    currentVolume = highVolume;
                    pauseTimer.start();
                } else {
                    super.pause();
                }
            }
        } catch (IllegalStateException e) {
            Log.v(TAG, "Illegal state");
        }
    }

    @Override
    public void start() {
        try {
            super.start();
            if (!SymphonyApplication.getInstance().getPreferenceUtils().getFade()) {
                try {
                    setVolume(highVolume, highVolume);
                } catch (IllegalStateException e) {
                    Log.v(TAG, "Illegal state");
                }
            } else {
                isPlaying = true;
                if (pauseTimer != null) {
                    pauseTimer.cancel();
                }
                if (resumeTimer != null) {
                    resumeTimer.start();
                }
            }
        } catch (IllegalStateException e) {
            Log.v(TAG, "Illegal state");
        }
    }

    @Override
    public boolean isPlaying() {
        if (!SymphonyApplication.getInstance().getPreferenceUtils().getFade()) {
            return super.isPlaying();
        } else {
            return isPlaying;
        }
    }

    public void end() {
        try {
            setVolume(highVolume, highVolume);
        } catch (IllegalStateException e) {
            Log.v(TAG, "Illegal state");
        }
        super.pause();
        try {
            reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (SymphonyApplication.getInstance().getPreferenceUtils().getFade()) {
            if (pauseTimer != null) {
                pauseTimer.cancel();
            }
            if (resumeTimer != null) {
                resumeTimer.cancel();
            }
        }
    }
}
