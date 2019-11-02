package music.symphony.com.materialmusicv2.utils.gaplessfadingmediaplayer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.AudioSessionIdChanged;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.gaplessfadingmediaplayer.fadingmediaplayer.FadingMediaPlayer;

public class GaplessFadingMediaPlayer {

    //For Multithreading
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    //2 FadingMediaPlayers for gapless/fading playback
    private FadingMediaPlayer currentMediaPlayer;
    private FadingMediaPlayer nextMediaPlayer;

    private boolean isNextMediaPlayerPreparing;

    //AudioManager
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener;

    //Listeners
    private MediaPlayer.OnErrorListener errorListener = null;
    private MediaPlayer.OnCompletionListener completionListener = null;
    private StateChangeListener stateChangeListener;

    //For Playing songs from a list
    private int currentPosition = -2;

    private void initCurrentMediaPlayer() {
        if (currentMediaPlayer == null) {
            currentMediaPlayer = new FadingMediaPlayer();
            currentMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            currentMediaPlayer.setOnCompletionListener(completionListener);
            currentMediaPlayer.setOnErrorListener(errorListener);
            EventBus.getDefault().post(new AudioSessionIdChanged());
        }
    }

    private void initNextMediaPlayer() {
        if (nextMediaPlayer == null) {
            nextMediaPlayer = new FadingMediaPlayer();
            nextMediaPlayer.setAudioSessionId(currentMediaPlayer.getAudioSessionId());
            nextMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            nextMediaPlayer.setOnCompletionListener(completionListener);
            nextMediaPlayer.setOnErrorListener(errorListener);
        }
    }

    public boolean isPlaying() {
        try {
            return currentMediaPlayer != null && currentMediaPlayer.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setVolume(float volume) {
        try {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.setVolume(volume, volume);
            }
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            if (requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                if (currentMediaPlayer != null) {
                    currentMediaPlayer.start();
                    if (stateChangeListener != null) {
                        stateChangeListener.onTrackResumed();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        try {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.pause();
            }
            if (stateChangeListener != null) {
                stateChangeListener.onTrackPaused();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.pause();
            }
            if (stateChangeListener != null) {
                stateChangeListener.onTrackStopped();
            }
            abandonAudioFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void seekTo(int position) {
        try {
            if (currentMediaPlayer != null) {
                currentMediaPlayer.seekTo(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCurrentPlaybackPosition() {
        try {
            if (currentMediaPlayer != null) {
                return currentMediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getAudioSessionId() {
        if (currentMediaPlayer != null) {
            return currentMediaPlayer.getAudioSessionId();
        } else {
            return -1;
        }
    }

    public void release() {
        if (currentMediaPlayer != null) {
            try {
                currentMediaPlayer.stop();
                currentMediaPlayer.reset();
            } catch (IllegalStateException ise) {
                ise.printStackTrace();
            }
            try {
                currentMediaPlayer.release();
                currentMediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (nextMediaPlayer != null) {
            try {
                nextMediaPlayer.stop();
                nextMediaPlayer.reset();
            } catch (IllegalStateException ise) {
                ise.printStackTrace();
            }
            try {
                nextMediaPlayer.release();
                nextMediaPlayer = null;
                isNextMediaPlayerPreparing = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stateChangeListener = null;
        audioManager = null;
        onAudioFocusChangeListener = null;
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public void setStateChangeListener(StateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public void setOnAudioFocusChangeListener(AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener) {
        this.onAudioFocusChangeListener = onAudioFocusChangeListener;
    }

    public void reset() {
        currentPosition = -2;
        isNextMediaPlayerPreparing = false;
    }

    private int getNextPosition(int position) {
        if (position + 1 >= SymphonyApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue()) {
            return 0;
        } else {
            return position + 1;
        }
    }

    private Song getNextSong(int position) {
        if (position + 1 >= SymphonyApplication.getInstance().getPlayingQueueManager().getNumberOfSongsInQueue()) {
            return SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().get(0);
        } else {
            return SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().get(position + 1);
        }
    }

    public void prepareTwoMediaPlayers(int position, boolean playAfterPreparing, boolean invokeOnTrackPaused) {
        if (stateChangeListener != null && invokeOnTrackPaused) {
            stateChangeListener.onTrackPaused();
        }
        prepareCurrentMediaPlayer(getCurrentSong(position), playAfterPreparing);
        prepareNextMediaPlayer(getNextSong(position));
    }

    public void prepareNextMediaPlayer(int position) {
        prepareNextMediaPlayer(getNextSong(position));
    }

    private void prepareCurrentMediaPlayer(final Song currentSong, final boolean playAfterPreparing) {
        if (currentSong == null) {
            return;
        }
        executorService.submit(() -> {
            try {
                initCurrentMediaPlayer();
                if (currentMediaPlayer != null) {
                    currentMediaPlayer.reset();
                }
            } catch (IllegalStateException ise) {
                ise.printStackTrace();
                try {
                    currentMediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentMediaPlayer = null;
                initCurrentMediaPlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (currentMediaPlayer != null) {
                    currentMediaPlayer.setDataSource(currentSong.getPath());
                    currentMediaPlayer.prepare();
                    if (playAfterPreparing) {
                        new Handler(Looper.getMainLooper()).post(() -> start());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void prepareNextMediaPlayer(final Song nextSong) {
        if (nextSong == null) {
            return;
        }
        executorService.submit(() -> {
            isNextMediaPlayerPreparing = true;
            try {
                initNextMediaPlayer();
                if (nextMediaPlayer != null) {
                    nextMediaPlayer.reset();
                }
            } catch (IllegalStateException ise) {
                ise.printStackTrace();
                try {
                    if (nextMediaPlayer != null) {
                        nextMediaPlayer.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nextMediaPlayer = null;
                initNextMediaPlayer();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (nextMediaPlayer != null) {
                    nextMediaPlayer.setDataSource(nextSong.getPath());
                    nextMediaPlayer.prepare();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            isNextMediaPlayerPreparing = false;
        });
    }

    public void playSong(int newPosition) {
        executorService.submit(() -> {
            final boolean needToInitAgain;
            if (newPosition == getNextPosition(currentPosition)) {
                if (!isNextMediaPlayerPreparing) {
                    setNextMediaPlayerAsCurrentMediaPlayerAndPlay();
                    needToInitAgain = false;
                } else {
                    needToInitAgain = true;
                }
            } else {
                needToInitAgain = true;
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                if (needToInitAgain) {
                    prepareTwoMediaPlayers(newPosition, true, true);
                } else {
                    start();
                }
                currentPosition = newPosition;
                prepareNextMediaPlayer(newPosition);
            });
        });
    }

    private void setNextMediaPlayerAsCurrentMediaPlayerAndPlay() {
        currentMediaPlayer.end();
        currentMediaPlayer = nextMediaPlayer;
        nextMediaPlayer = null;
    }

    private Song getCurrentSong(int position) {
        try {
            return SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().get(position);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    private int requestAudioFocus() {
        if (audioManager != null && onAudioFocusChangeListener != null) {
            return audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        } else {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void abandonAudioFocus() {
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
    }

    public interface StateChangeListener {

        void onTrackResumed();

        void onTrackPaused();

        void onTrackStopped();
    }
}