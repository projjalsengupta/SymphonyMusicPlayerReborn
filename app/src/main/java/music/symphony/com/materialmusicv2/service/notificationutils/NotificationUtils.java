package music.symphony.com.materialmusicv2.service.notificationutils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.MainActivity;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.objects.events.FavoriteChanged;
import music.symphony.com.materialmusicv2.objects.events.MediaSessionData;
import music.symphony.com.materialmusicv2.objects.events.MediaStates;
import music.symphony.com.materialmusicv2.service.MusicService;
import music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.isCurrentSongFavorite;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.CHANNEL_MEDIA_CONTROLS;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFICATION_ID;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_FAVORITE;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_SKIP2SONGS;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_SKIP3SONGS;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.NOTIFY_STOP;

public class NotificationUtils implements SharedPreferences.OnSharedPreferenceChangeListener {

    private MusicService musicService;
    private Context context;

    private int width;
    private int height;

    private NotificationCompat.Builder notificationBuilder;

    private RemoteViews bigContentView;
    private RemoteViews smallContentView;

    private PendingIntent favoriteIntent;
    private PendingIntent deleteIntent;
    private PendingIntent skip2songs;
    private PendingIntent skip3songs;
    private PendingIntent contentIntent;

    private ExecutorService executorService;

    public synchronized void init(MusicService musicService, Context context) {
        if (musicService == null || context == null) {
            return;
        }
        this.musicService = musicService;
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        NotificationManager notificationManager = (NotificationManager) musicService.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                createNotificationChannels(notificationManager);
            }
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        height = Math.round(128 * context.getResources().getDisplayMetrics().density);
        width = Math.round(128 * context.getResources().getDisplayMetrics().density);
        Intent favoriteIntent = new Intent(NOTIFY_FAVORITE);
        Intent deleteIntent = new Intent(NOTIFY_STOP);
        Intent skip2songsIntent = new Intent(NOTIFY_SKIP2SONGS);
        Intent skip3songsIntent = new Intent(NOTIFY_SKIP3SONGS);
        this.favoriteIntent = PendingIntent.getBroadcast(context, 0, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.skip2songs = PendingIntent.getBroadcast(context, 0, skip2songsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        this.skip3songs = PendingIntent.getBroadcast(context, 0, skip3songsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("openNowPlaying", true);
        contentIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public synchronized void nullify() {
        stopNotification();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        musicService = null;
        context = null;

        notificationBuilder = null;

        bigContentView = null;
        smallContentView = null;

        favoriteIntent = null;
        skip2songs = null;
        skip3songs = null;

        executorService = null;
    }

    private synchronized void buildNotification(MediaSessionData mediaSessionData) {
        if (executorService != null) {
            executorService.execute(() -> {
                try {
                    boolean favorite = isCurrentSongFavorite(context);
                    int backgroundColor = mediaSessionData.albumArt.backgroundColor;
                    final Bitmap notificationBitmap = Bitmap.createScaledBitmap(mediaSessionData.albumArt.albumArt, width, height, false);
                    if (!SymphonyApplication.getInstance().getPreferenceUtils().getUseMediaStyleNotification()) {
                        bigContentView = new RemoteViews(context.getPackageName(), R.layout.app_notification_big);
                        smallContentView = new RemoteViews(context.getPackageName(), R.layout.app_notification_small);
                        int foregroundColor = mediaSessionData.albumArt.foregroundColor;
                        bigContentView.setOnClickPendingIntent(R.id.playPrevious, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
                        bigContentView.setOnClickPendingIntent(R.id.playPause, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE));
                        bigContentView.setOnClickPendingIntent(R.id.playNext, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
                        bigContentView.setOnClickPendingIntent(R.id.favoriteButton, favoriteIntent);
                        bigContentView.setOnClickPendingIntent(R.id.queue1, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
                        bigContentView.setOnClickPendingIntent(R.id.queue2, skip2songs);
                        bigContentView.setOnClickPendingIntent(R.id.queue3, skip3songs);
                        bigContentView.setImageViewBitmap(R.id.albumArt, notificationBitmap);
                        setColorFilter(bigContentView, backgroundColor, R.id.albumArtGradient);
                        bigContentView.setTextViewText(R.id.songName, mediaSessionData.currentPlayingSong.songName);
                        bigContentView.setTextViewText(R.id.artistName, mediaSessionData.currentPlayingSong.artistName);
                        bigContentView.setTextViewText(R.id.albumName, mediaSessionData.currentPlayingSong.albumName);
                        setQueue();
                        bigContentView.setInt(R.id.notificationBackground, "setBackgroundColor", backgroundColor);
                        setTextColor(bigContentView, foregroundColor,
                                R.id.songName,
                                R.id.artistName,
                                R.id.albumName,
                                R.id.symphonyText
                        );
                        setColorFilter(bigContentView, foregroundColor,
                                R.id.symphonyImage,
                                R.id.circle,
                                R.id.playPrevious,
                                R.id.playPause,
                                R.id.playNext,
                                R.id.favoriteButton
                        );
                        bigContentView.setTextColor(R.id.queue1, backgroundColor);
                        bigContentView.setTextColor(R.id.queue2, backgroundColor);
                        bigContentView.setTextColor(R.id.queue3, backgroundColor);
                        bigContentView.setInt(R.id.queue, "setBackgroundColor", foregroundColor);
                        bigContentView.setTextColor(R.id.queue2, backgroundColor);
                        bigContentView.setTextColor(R.id.queue3, backgroundColor);
                        bigContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(playbackState == PlaybackStateCompat.STATE_PLAYING));
                        bigContentView.setImageViewResource(R.id.favoriteButton, DrawableUtils.getFavoriteResourceForNotification(favorite));
                        smallContentView.setOnClickPendingIntent(R.id.playPrevious, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
                        smallContentView.setOnClickPendingIntent(R.id.playPause, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE));
                        smallContentView.setOnClickPendingIntent(R.id.playNext, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
                        smallContentView.setOnClickPendingIntent(R.id.favoriteButton, favoriteIntent);
                        smallContentView.setImageViewBitmap(R.id.albumArt, notificationBitmap);
                        smallContentView.setInt(R.id.albumArtGradient, "setColorFilter", backgroundColor);
                        smallContentView.setTextViewText(R.id.songName, mediaSessionData.currentPlayingSong.songName);
                        smallContentView.setTextViewText(R.id.artistName, mediaSessionData.currentPlayingSong.artistName);
                        smallContentView.setTextViewText(R.id.albumName, mediaSessionData.currentPlayingSong.albumName);
                        smallContentView.setInt(R.id.notificationBackground, "setBackgroundColor", backgroundColor);
                        setTextColor(smallContentView, foregroundColor,
                                R.id.songName,
                                R.id.artistName,
                                R.id.albumName,
                                R.id.symphonyText
                        );
                        setColorFilter(smallContentView, foregroundColor,
                                R.id.symphonyImage,
                                R.id.circle,
                                R.id.playPrevious,
                                R.id.playPause,
                                R.id.playNext
                        );
                        smallContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(playbackState == PlaybackStateCompat.STATE_PLAYING));
                        notificationBuilder = new NotificationCompat
                                .Builder(musicService, CHANNEL_MEDIA_CONTROLS)
                                .setShowWhen(false)
                                .setSmallIcon(R.drawable.ic_statusbar)
                                .setCustomContentView(smallContentView)
                                .setCustomBigContentView(bigContentView)
                                .setContentIntent(contentIntent)
                                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                    } else {
                        notificationBuilder = new NotificationCompat
                                .Builder(musicService, CHANNEL_MEDIA_CONTROLS)
                                .setContentTitle(mediaSessionData.currentPlayingSong.songName)
                                .setContentText(mediaSessionData.currentPlayingSong.artistName)
                                .setSubText(mediaSessionData.currentPlayingSong.albumName)
                                .setSmallIcon(R.drawable.ic_statusbar)
                                .setLargeIcon(notificationBitmap)
                                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                                .setContentIntent(contentIntent)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setOngoing(playbackState == PlaybackStateCompat.STATE_PLAYING)
                                .addAction(R.drawable.ic_skip_previous, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                                .addAction(playbackState == PlaybackStateCompat.STATE_PLAYING ? R.drawable.ic_pause : R.drawable.ic_play, "Play / Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE))
                                .addAction(R.drawable.ic_skip_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
                                .addAction(favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border, "Favorite", favoriteIntent)
                                .setColorized(true)
                                .setShowWhen(false)
                                .setPriority(NotificationCompat.PRIORITY_MAX);
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            notificationBuilder.setColor(backgroundColor);
                        }
                        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setShowCancelButton(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                                .setCancelButtonIntent(deleteIntent)
                                .setMediaSession(SymphonyApplication.getInstance().getMediaSessionUtils().getMediaSession().getSessionToken()));
                    }
                    showNotification(notificationBuilder.build(), playbackState);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private synchronized void showNotification(final Notification notification, int playbackState) {
        if (playbackState == PlaybackStateCompat.STATE_STOPPED) {
            stopNotification();
        } else {
            try {
                if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                    startForeground(notification);
                } else {
                    musicService.stopForeground(false);
                    NotificationManager notificationManager = (NotificationManager) musicService.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.notify(NOTIFICATION_ID, notification);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void stopNotification() {
        if (musicService != null) {
            musicService.stopForeground(true);
        }
    }

    private synchronized void startForeground(final Notification notification) {
        try {
            Intent intent = new Intent(context, MusicService.class);
            ContextCompat.startForegroundService(context, intent);
            musicService.startForeground(NOTIFICATION_ID, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannels(NotificationManager notificationManager) {
        NotificationChannel mediaControlsChannel = new NotificationChannel(CHANNEL_MEDIA_CONTROLS,
                "Music",
                NotificationManager.IMPORTANCE_MIN);
        mediaControlsChannel.setShowBadge(false);
        mediaControlsChannel.enableLights(false);
        mediaControlsChannel.enableVibration(false);
        mediaControlsChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(mediaControlsChannel);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(MediaSessionData mediaSessionData) {
        buildNotification(mediaSessionData);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FavoriteChanged favoriteChanged) {
        updateActions(favoriteChanged.favorite);
    }

    private int playbackState = PlaybackStateCompat.STATE_NONE;

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(MediaStates mediaStates) {
        int playbackState = mediaStates.playbackState ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        if (this.playbackState != playbackState) {
            this.playbackState = playbackState;
            updateActions(playbackState);
        }
    }

    private synchronized void updateActions(int playbackState) {
        if (executorService != null) {
            executorService.execute(() -> {
                try {
                    if (SymphonyApplication.getInstance().getPreferenceUtils().getUseMediaStyleNotification()) {
                        boolean favorite = isCurrentSongFavorite(context);
                        notificationBuilder.mActions.clear();
                        notificationBuilder
                                .addAction(R.drawable.ic_skip_previous, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                                .addAction(DrawableUtils.getPlayPauseResourceForNotificationAndWidget(playbackState == PlaybackStateCompat.STATE_PLAYING), "Play / Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE))
                                .addAction(R.drawable.ic_skip_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
                                .addAction(favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border, "Favorite", favoriteIntent)
                                .setOngoing(playbackState == PlaybackStateCompat.STATE_PLAYING);
                        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setShowCancelButton(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                                .setCancelButtonIntent(deleteIntent)
                                .setMediaSession(SymphonyApplication.getInstance().getMediaSessionUtils().getMediaSession().getSessionToken()));
                    } else {
                        setQueue();
                        bigContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(playbackState == PlaybackStateCompat.STATE_PLAYING));
                        smallContentView.setImageViewResource(R.id.playPause, DrawableUtils.getPlayPauseResourceForNotificationAndWidget(playbackState == PlaybackStateCompat.STATE_PLAYING));
                    }
                    showNotification(notificationBuilder.build(), playbackState);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private synchronized void updateActions(boolean favorite) {
        if (executorService != null) {
            executorService.execute(() -> {
                try {
                    if (SymphonyApplication.getInstance().getPreferenceUtils().getUseMediaStyleNotification()) {
                        notificationBuilder.mActions.clear();
                        notificationBuilder
                                .addAction(R.drawable.ic_skip_previous, "Previous", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                                .addAction(DrawableUtils.getPlayPauseResourceForNotificationAndWidget(playbackState == PlaybackStateCompat.STATE_PLAYING), "Play / Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY_PAUSE))
                                .addAction(R.drawable.ic_skip_next, "Next", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
                                .addAction(favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border, "Favorite", favoriteIntent)
                                .setOngoing(playbackState == PlaybackStateCompat.STATE_PLAYING);
                        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setShowCancelButton(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                                .setCancelButtonIntent(deleteIntent)
                                .setMediaSession(SymphonyApplication.getInstance().getMediaSessionUtils().getMediaSession().getSessionToken()));
                    } else {
                        setQueue();
                        bigContentView.setImageViewResource(R.id.favoriteButton, DrawableUtils.getFavoriteResourceForNotification(favorite));
                    }
                    try {
                        showNotification(notificationBuilder.build(), playbackState);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private synchronized void reset() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private synchronized void setQueue() {
        if (!SymphonyApplication.getInstance().getPreferenceUtils().getUseMediaStyleNotification()) {
            ArrayList<Song> songs = SymphonyApplication.getInstance().getPlayingQueueManager().getSongs();
            int songPosition = SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition();
            if (songs != null && !songs.isEmpty() && SymphonyApplication.getInstance().getPreferenceUtils().getShowQueueInNotification()) {
                bigContentView.setViewVisibility(R.id.queue, View.VISIBLE);
                if (songs.size() - 1 - songPosition == 1) {
                    bigContentView.setViewVisibility(R.id.queue1, View.VISIBLE);
                    bigContentView.setViewVisibility(R.id.queue2, View.GONE);
                    bigContentView.setViewVisibility(R.id.queue3, View.GONE);
                    bigContentView.setTextViewText(R.id.queue1, songs.get(songPosition + 1).getName());
                } else if (songs.size() - 1 - songPosition == 2) {
                    bigContentView.setViewVisibility(R.id.queue1, View.VISIBLE);
                    bigContentView.setViewVisibility(R.id.queue2, View.VISIBLE);
                    bigContentView.setViewVisibility(R.id.queue3, View.GONE);
                    bigContentView.setTextViewText(R.id.queue1, songs.get(songPosition + 1).getName());
                    bigContentView.setTextViewText(R.id.queue2, songs.get(songPosition + 2).getName());
                } else if (songs.size() - 1 - songPosition >= 3) {
                    bigContentView.setViewVisibility(R.id.queue1, View.VISIBLE);
                    bigContentView.setViewVisibility(R.id.queue2, View.VISIBLE);
                    bigContentView.setViewVisibility(R.id.queue3, View.VISIBLE);
                    bigContentView.setTextViewText(R.id.queue1, songs.get(songPosition + 1).getName());
                    bigContentView.setTextViewText(R.id.queue2, songs.get(songPosition + 2).getName());
                    bigContentView.setTextViewText(R.id.queue3, songs.get(songPosition + 3).getName());
                } else {
                    bigContentView.setViewVisibility(R.id.queue, View.GONE);
                }
            } else {
                bigContentView.setViewVisibility(R.id.queue, View.GONE);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "useMediaStyleNotification": {
                reset();
                break;
            }
            case "showQueueInNotification": {
                reset();
                break;
            }
        }
    }
}