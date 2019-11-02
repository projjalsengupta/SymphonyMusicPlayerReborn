package music.symphony.com.materialmusicv2.utils.mediasessionutils;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.objects.events.AlbumArt;
import music.symphony.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.symphony.com.materialmusicv2.objects.events.MediaSessionData;
import music.symphony.com.materialmusicv2.objects.events.MediaStates;
import music.symphony.com.materialmusicv2.objects.events.PlaybackPosition;
import music.symphony.com.materialmusicv2.objects.events.PlaybackState;
import music.symphony.com.materialmusicv2.objects.events.RepeatState;
import music.symphony.com.materialmusicv2.objects.events.ShuffleState;
import music.symphony.com.materialmusicv2.objects.events.SongPositionInQueue;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.SendCurrentPlaybackPosition;
import music.symphony.com.materialmusicv2.service.MediaButtonBroadcastReceiver;
import music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.symphony.com.materialmusicv2.utils.bitmaputils.BlurBuilder;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.drawBitmapBackground;
import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.getDominantColors;

public class MediaSessionUtils {

    private Bitmap currentAlbumArt = null;
    private Bitmap currentBlurredAlbumArt = null;
    private int currentBackgroundColor = Color.BLACK;
    private int currentForegroundColor = Color.WHITE;

    private static final String TAG = "mediasessionutils";

    private MediaSessionCompat mediaSession;

    public MediaSessionUtils(Context context) {
        initMediaSession(context);
        startTimer();
    }

    private final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    private void initMediaSession(Context context) {
        if (context == null) {
            return;
        }
        mediaSession = new MediaSessionCompat(
                context,
                "Symphony",
                new ComponentName(context,
                        MediaButtonBroadcastReceiver.class),
                null);
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    public void setCallback(MediaSessionCompat.Callback callback) {
        if (mediaSession == null) {
            return;
        }
        mediaSession.setCallback(callback);
    }

    private int playbackState = PlaybackStateCompat.STATE_STOPPED;

    public void setPlaybackState(final int state, final int playbackPosition) {
        if (mediaSession == null) {
            return;
        }
        playbackState = state;
        MediaSessionTasker.addTask(() -> mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(state, playbackPosition, 1.0f)
                .setActions(MEDIA_SESSION_ACTIONS)
                .build()));

        EventBus.getDefault().removeStickyEvent(PlaybackState.class);
        EventBus.getDefault().postSticky(new PlaybackState(state == PlaybackStateCompat.STATE_PLAYING));

        EventBus.getDefault().removeStickyEvent(MediaStates.class);
        EventBus.getDefault().postSticky(new MediaStates(mediaSession.getController().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL, mediaSession.getController().getRepeatMode(), state == PlaybackStateCompat.STATE_PLAYING));
    }

    /**
     * Playback position and duration
     */

    private int playbackPosition = 0;
    private int duration = 0;

    private Runnable progressUpdater = () -> {
        if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
            playbackPosition += 1000;
        }

        EventBus.getDefault().removeStickyEvent(PlaybackPosition.class);
        EventBus.getDefault().postSticky(new PlaybackPosition(playbackPosition, duration));
    };

    private void startTimer() {
        EventBus.getDefault().post(new SendCurrentPlaybackPosition());
        TaskRepeater.getInstance().addTask(progressUpdater, 1000);
    }

    private void stopTimer() {
        TaskRepeater.getInstance().removeTask(progressUpdater);
    }

    public void setPlaybackPosition(int playbackPosition) {
        try {
            if (mediaSession == null) {
                return;
            }
            this.playbackPosition = playbackPosition;
            MediaSessionTasker.addTask(() -> {
                int state = mediaSession.getController().getPlaybackState().getState();
                mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(state, playbackPosition, 1.0f)
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .build());
            });

            EventBus.getDefault().removeStickyEvent(PlaybackPosition.class);
            EventBus.getDefault().postSticky(new PlaybackPosition(playbackPosition, duration));
        } catch (NullPointerException e) {
            Log.e(TAG, "controller or playbackstate or metadata is null!");
        }
    }

    public void setRepeat(int repeat) {
        if (mediaSession == null) {
            return;
        }
        MediaSessionTasker.addTask(() -> mediaSession.setRepeatMode(repeat));

        EventBus.getDefault().removeStickyEvent(RepeatState.class);
        EventBus.getDefault().postSticky(new RepeatState(repeat));

        EventBus.getDefault().removeStickyEvent(MediaStates.class);
        EventBus.getDefault().postSticky(new MediaStates(mediaSession.getController().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL, repeat, playbackState == PlaybackStateCompat.STATE_PLAYING));
    }

    public void setShuffle(int shuffle) {
        if (mediaSession == null) {
            return;
        }
        MediaSessionTasker.addTask(() -> mediaSession.setShuffleMode(shuffle));

        EventBus.getDefault().removeStickyEvent(ShuffleState.class);
        EventBus.getDefault().postSticky(new ShuffleState(shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL));

        EventBus.getDefault().removeStickyEvent(MediaStates.class);
        EventBus.getDefault().postSticky(new MediaStates(shuffle == PlaybackStateCompat.SHUFFLE_MODE_ALL, mediaSession.getController().getRepeatMode(), playbackState == PlaybackStateCompat.STATE_PLAYING));
    }

    private Future<?> metadataTask = null;

    public void setMetadata(final Context context, final Song song, final int songPosition, final int numberOfSongs) {
        if (mediaSession == null || context == null || song == null) {
            return;
        }
        playbackPosition = 0;
        duration = song.getDuration();
        if (metadataTask != null) {
            try {
                metadataTask.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        metadataTask = MediaSessionTasker.addTask(() -> {
            if (!Thread.interrupted()) {
                final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.getArtist())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getName())
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration())
                        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, songPosition + 1)
                        .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.getDate())
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, numberOfSongs);
                }
                if (!Thread.interrupted()) {
                    int width = context.getResources().getDisplayMetrics().widthPixels;
                    int height = context.getResources().getDisplayMetrics().heightPixels;
                    try {
                        currentAlbumArt = Glide.with(context)
                                .asBitmap()
                                .load(song)
                                .apply(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                                .submit(width, height)
                                .get();
                    } catch (ExecutionException | InterruptedException e) {
                        currentAlbumArt = BitmapUtils.decodeResource(context.getResources(), R.drawable.ic_blank_album_art, width, height, Bitmap.Config.ARGB_8888);
                        int theme = SymphonyApplication.getInstance().getPreferenceUtils().getTheme();
                        int backgroundColor = theme == 0 ? Color.WHITE : theme == 2 ? Color.BLACK : Color.parseColor("#212121");
                        currentAlbumArt = drawBitmapBackground(currentAlbumArt, backgroundColor);
                    }
                    currentBlurredAlbumArt = BlurBuilder.blur(context, currentAlbumArt);
                    int[] colors = getDominantColors(currentAlbumArt);
                    currentBackgroundColor = colors[0];
                    currentForegroundColor = colors[1];
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, currentAlbumArt);
                    MediaMetadataCompat metadata = metadataBuilder.build();
                    mediaSession.setMetadata(metadata);
                    mediaSession.setActive(true);

                    EventBus.getDefault().removeStickyEvent(AlbumArt.class);
                    EventBus.getDefault().postSticky(new AlbumArt(
                            currentAlbumArt,
                            currentBackgroundColor,
                            currentForegroundColor
                    ));

                    EventBus.getDefault().removeStickyEvent(MediaSessionData.class);
                    EventBus.getDefault().postSticky(new MediaSessionData(
                            new AlbumArt(currentAlbumArt,
                                    currentBackgroundColor,
                                    currentForegroundColor),
                            new MediaStates(mediaSession.getController().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL, mediaSession.getController().getRepeatMode(), playbackState == PlaybackStateCompat.STATE_PLAYING),
                            new CurrentPlayingSong(song.getName(), song.getArtist(), song.getAlbum())
                    ));
                }
            }
        });

        EventBus.getDefault().removeStickyEvent(CurrentPlayingSong.class);
        EventBus.getDefault().postSticky(new CurrentPlayingSong(song.getName(), song.getArtist(), song.getAlbum()));

        EventBus.getDefault().removeStickyEvent(PlaybackPosition.class);
        EventBus.getDefault().postSticky(new PlaybackPosition(playbackPosition, duration));

        EventBus.getDefault().removeStickyEvent(SongPositionInQueue.class);
        EventBus.getDefault().postSticky(new SongPositionInQueue(songPosition));
    }

    public void setQueue(List<MediaSessionCompat.QueueItem> queue) {
        if (mediaSession == null || queue == null) {
            return;
        }
        MediaSessionTasker.addTask(() -> {
            mediaSession.setQueue(queue);
            mediaSession.setQueueTitle("Queue");
        });
    }

    public void release() {
        stopTimer();
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }

        MediaSessionTasker.shutdown();
    }
}