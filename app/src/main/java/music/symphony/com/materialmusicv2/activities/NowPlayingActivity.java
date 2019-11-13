package music.symphony.com.materialmusicv2.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import com.gauravk.audiovisualizer.model.AnimSpeed;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.CircleLineVisualizer;
import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomSheetLyricsFragment;
import music.symphony.com.materialmusicv2.customviews.nowplaying.NowPlayingBackground;
import music.symphony.com.materialmusicv2.customviews.nowplaying.NowPlayingLyrics;
import music.symphony.com.materialmusicv2.customviews.others.SquareImageView;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.objects.events.AlbumArt;
import music.symphony.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.symphony.com.materialmusicv2.objects.events.FavoriteChanged;
import music.symphony.com.materialmusicv2.objects.events.PlaybackPosition;
import music.symphony.com.materialmusicv2.objects.events.PlaybackState;
import music.symphony.com.materialmusicv2.objects.events.RepeatState;
import music.symphony.com.materialmusicv2.objects.events.ShuffleState;
import music.symphony.com.materialmusicv2.objects.events.SongListChanged;
import music.symphony.com.materialmusicv2.objects.events.SongPositionInQueue;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.LinkVisualizer;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.UnlinkVisualizer;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.VisualizerData;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.VisualizerTypeChanged;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils.getRoundedDrawable;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addCurrentSongToFavorite;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.changeRepeat;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.changeShuffle;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.clearQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.getCurrentSong;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.getSongPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.isCurrentSongFavorite;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playAtSongPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.removeCurrentSongFromFavorite;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.seekTo;
import static music.symphony.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showInputDialog;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils.getPlayPauseResourceBlack;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DrawableUtils.getRepeatResourceBlack;
import static music.symphony.com.materialmusicv2.utils.lyricsutils.LyricsUtils.getLyrics;
import static music.symphony.com.materialmusicv2.utils.lyricsutils.LyricsUtils.setLyricsInCache;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.dipToPixels;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.getRealString;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.treeUri;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.createPlaylist;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class NowPlayingActivity extends MusicPlayerActivity implements SharedPreferences.OnSharedPreferenceChangeListener, NowPlayingLyrics.ClickEventListener, NowPlayingBackground.ScrollListener {

    @Nullable
    @BindView(R.id.albumArt)
    SquareImageView albumArt;
    @Nullable
    @BindView(R.id.nowPlayingBackground)
    NowPlayingBackground nowPlayingBackground;
    @BindView(R.id.container)
    View container;
    @BindView(R.id.songName)
    TextView songName;
    @Nullable
    @BindView(R.id.artistNameAndAlbumName)
    TextView artistNameAndAlbumName;
    @Nullable
    @BindView(R.id.lapsedTime)
    TextView lapsedTime;
    @Nullable
    @BindView(R.id.lapsedTime2)
    TextView lapsedTime2;
    @Nullable
    @BindView(R.id.totalDuration)
    TextView totalDuration;
    @BindView(R.id.playPrevious)
    ImageButton playPrevious;
    @BindView(R.id.playPause)
    ExtendedFloatingActionButton playPause;
    @BindView(R.id.playNext)
    ImageButton playNext;
    @BindView(R.id.shuffle)
    ImageButton shuffle;
    @BindView(R.id.menu)
    ImageButton menu;
    @BindView(R.id.repeat)
    ImageButton repeat;
    @BindView(R.id.favoriteButton)
    ImageButton favoriteButton;
    @BindView(R.id.lyricsButton)
    ImageButton lyricsButton;
    @BindView(R.id.openQueue)
    ImageButton openQueue;
    @Nullable
    @BindView(R.id.songProgress)
    CircularSeekBar songProgress;
    @Nullable
    @BindView(R.id.songProgress2)
    SeekBar songProgress2;
    @Nullable
    @BindView(R.id.waveVisualizer)
    WaveVisualizer waveVisualizer;
    @Nullable
    @BindView(R.id.barVisualizer)
    BarVisualizer barVisualizer;
    @Nullable
    @BindView(R.id.circleLineVisualizer)
    CircleLineVisualizer circleLineVisualizer;


    @OnClick({R.id.playPause, R.id.playPrevious, R.id.playNext, R.id.repeat, R.id.shuffle, R.id.favoriteButton, R.id.lyricsButton, R.id.menu, R.id.openQueue})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playPause: {
                pauseOrResumePlayer();
                break;
            }
            case R.id.playPrevious: {
                playPrevious();
                break;
            }
            case R.id.playNext: {
                playNext();
                break;
            }
            case R.id.repeat: {
                changeRepeat();
                break;
            }
            case R.id.shuffle: {
                changeShuffle();
                break;
            }
            case R.id.openQueue: {
                Intent intent = new Intent(NowPlayingActivity.this, QueueActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.favoriteButton: {
                createPlaylist("Favorites", getApplicationContext());
                if (isCurrentSongFavorite(getApplicationContext())) {
                    removeCurrentSongFromFavorite(getApplicationContext());
                } else {
                    addCurrentSongToFavorite(getApplicationContext());
                }
                break;
            }
            case R.id.lyricsButton: {
                showLyrics();
                break;
            }
            case R.id.menu: {
                if (menu == null) {
                    return;
                }
                PopupMenu popupMenu = new PopupMenu(menu.getContext(), menu);
                popupMenu.getMenuInflater().inflate(R.menu.menu_now_playing_song, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    Song song = getCurrentSong();
                    switch (item.getItemId()) {
                        case R.id.action_add_to_playlist: {
                            showAddToPlaylistDialog(NowPlayingActivity.this, song, false);
                            break;
                        }
                        case R.id.action_share: {
                            shareSong(NowPlayingActivity.this, song.getPath());
                            break;
                        }
                        case R.id.action_add_queue_to_playlist: {
                            showAddToPlaylistDialog(NowPlayingActivity.this, SymphonyApplication.getInstance().getPlayingQueueManager().getSongs());
                            break;
                        }
                        case R.id.action_go_to_album: {
                            Bundle bundle = new Bundle();
                            bundle.putString("ALBUM_NAME", song.getAlbum());
                            bundle.putString("ARTIST_NAME", song.getArtist());
                            bundle.putLong("ALBUM_ID", song.getAlbumId());
                            Intent intent = new Intent(NowPlayingActivity.this, AlbumActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            break;
                        }
                        case R.id.action_go_to_artist: {
                            Intent intent = new Intent(NowPlayingActivity.this, ArtistActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("TITLE", song.getArtist());
                            bundle.putLong("ID", song.getArtistId());
                            intent.putExtras(bundle);
                            startActivity(intent);
                            break;
                        }
                        case R.id.action_edit: {
                            Intent intent = new Intent(NowPlayingActivity.this, EditMetaDataActivity.class);
                            intent.putExtra("PATH", getCurrentSong().getPath());
                            intent.putExtra("ID", getCurrentSong().getId());
                            intent.putExtra("AlBUM_ID", getCurrentSong().getAlbumId());
                            startActivity(intent);
                            break;
                        }
                        case R.id.action_equalizer: {
                            Intent equalizerIntent = new Intent(NowPlayingActivity.this, EqualizerActivity.class);
                            startActivity(equalizerIntent);
                            break;
                        }
                        case R.id.action_set_as_ringtone: {
                            setAudioAsRingtone(new File(song.getPath()), getApplicationContext());
                            break;
                        }
                        case R.id.action_clear_queue: {
                            clearQueue(SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition());
                            break;
                        }
                    }
                    return true;
                });
                popupMenu.show();
                break;
            }
        }
    }

    private BottomSheetLyricsFragment bottomSheetLyricsFragment;
    private NowPlayingLyrics nowPlayingLyrics;

    private int nowPlayingStyle = 1;

    private ObjectAnimator rotation;

    private boolean wasPlaying = false;
    private boolean hasRotationStarted = false;

    private boolean isUserTouchingSeekBar = false;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ValueAnimator colorAnimation;
    private int previousBackgroundColor = Color.BLACK;

    private int visualizerType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nowPlayingStyle = SymphonyApplication.getInstance().getPreferenceUtils().getNowPlayingStyle();
        setContentView((nowPlayingStyle == 0 || nowPlayingStyle == 5 || nowPlayingStyle == 6) ? R.layout.activity_now_playing_one : (nowPlayingStyle == 2 ? R.layout.activity_now_playing_three : R.layout.activity_now_playing_two));

        setTheme(ThemeUtils.getTheme(this));

        ButterKnife.bind(this);

        setUpRotation();
        setUpNowPlayingBackground();
        setUpCircularSeekBar();
        setUpSeekBar();
        setUpVisualizer();
    }

    private void setUpNowPlayingBackground() {
        if (nowPlayingBackground != null) {
            nowPlayingBackground.setScrollListener(this);
        }
    }

    private void setUpCircularSeekBar() {
        if (songProgress != null) {
            songProgress.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                @Override
                public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                    if (fromUser) {
                        seekTo((int) progress);
                    }
                }

                @Override
                public void onStopTrackingTouch(CircularSeekBar seekBar) {
                    isUserTouchingSeekBar = false;
                }

                @Override
                public void onStartTrackingTouch(CircularSeekBar seekBar) {
                    isUserTouchingSeekBar = true;
                }
            });
        }
    }

    private void setUpSeekBar() {
        if (songProgress2 != null) {
            songProgress2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isUserTouchingSeekBar = true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isUserTouchingSeekBar = false;
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (SymphonyApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
            EventBus.getDefault().post(new UnlinkVisualizer());

            if (waveVisualizer != null) {
                waveVisualizer.hide();
            }
            if (barVisualizer != null) {
                barVisualizer.hide();
            }
            if (circleLineVisualizer != null) {
                circleLineVisualizer.setVisibility(View.INVISIBLE);
            }
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        if (wasPlaying && rotation != null) {
            rotation.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (SymphonyApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
            EventBus.getDefault().post(new LinkVisualizer());

            if (visualizerType == 0) {
                if (circleLineVisualizer != null) {
                    circleLineVisualizer.setVisibility(View.VISIBLE);
                }
            } else if (visualizerType == 1) {
                if (barVisualizer != null) {
                    barVisualizer.show();
                }
            } else {
                if (waveVisualizer != null) {
                    waveVisualizer.show();
                }
            }
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (wasPlaying && rotation != null) {
            rotation.resume();
        }
    }

    @Override
    public void onDestroy() {
        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();

        if (colorAnimation != null) {
            colorAnimation.cancel();
            colorAnimation.removeAllUpdateListeners();
            colorAnimation.removeAllListeners();
        }

        if (rotation != null) {
            rotation.cancel();
            rotation.removeAllListeners();
            rotation = null;
        }
        if (SymphonyApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
            if (waveVisualizer != null) {
                waveVisualizer.release();
            }
            if (circleLineVisualizer != null) {
                circleLineVisualizer.release();
            }
            if (barVisualizer != null) {
                barVisualizer.release();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 0) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == 42) {
            treeUri = data.getData();
            final SharedPreferences.Editor editor = getSharedPreferences("mypref", MODE_PRIVATE).edit();
            editor.putString("treeUri", treeUri.toString());
            editor.apply();
            grantUriPermission(getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(AlbumArt albumArt) {
        setAlbumArt(albumArt.albumArt);
        if (SymphonyApplication.getInstance().getPreferenceUtils().getColorizeElementsAccordingToAlbumArt()) {
            setColors(albumArt.backgroundColor, albumArt.foregroundColor);
        } else {
            setColors(ThemeUtils.getThemePrimaryColor(NowPlayingActivity.this), ThemeUtils.getThemeAccentColor(NowPlayingActivity.this));
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongPositionInQueue songPositionInQueue) {
        updateFavoriteButton();
        setLyrics();
        scrollTo(songPositionInQueue.position);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongListChanged songListChanged) {
        updateQueue(songListChanged);
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
    public void onEvent(RepeatState repeatState) {
        setRepeat(repeatState.state);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ShuffleState shuffleState) {
        setShuffle(shuffleState.state);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong) {
        setSongName(currentPlayingSong.songName);
        setArtistNameAndAlbumName(currentPlayingSong.artistName, currentPlayingSong.albumName);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VisualizerData visualizerData) {
        if (SymphonyApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
            if (visualizerType == 0) {
                if (circleLineVisualizer != null) {
                    circleLineVisualizer.setRawAudioBytes(visualizerData.data);
                }
            } else if (visualizerType == 1) {
                if (barVisualizer != null) {
                    barVisualizer.setRawAudioBytes(visualizerData.data);
                }
            } else {
                if (waveVisualizer != null) {
                    waveVisualizer.setRawAudioBytes(visualizerData.data);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FavoriteChanged favoriteChanged) {
        setFavorite(favoriteChanged.favorite);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VisualizerTypeChanged visualizerTypeChanged) {
        visualizerType = visualizerTypeChanged.visualizerType;
    }

    private void setArtistNameAndAlbumName(String artistName, String albumName) {
        if (artistNameAndAlbumName != null) {
            artistNameAndAlbumName.setText(String.format("%s • %s", artistName, albumName));
            artistNameAndAlbumName.setSelected(true);
        }
    }

    private void setSongProgress(int progress, int maxProgress) {
        final String lapsedText = covertMilisToTimeString(progress);
        final String durationText = covertMilisToTimeString(maxProgress);

        if (lapsedTime != null) {
            lapsedTime.setText(String.format("%s/%s", lapsedText, durationText));
        }
        if (lapsedTime2 != null) {
            lapsedTime2.setText(lapsedText);
        }
        if (totalDuration != null) {
            totalDuration.setText(durationText);
        }
        if (isUserTouchingSeekBar) {
            return;
        }
        if (songProgress != null) {
            if (songProgress.getMax() != maxProgress) {
                songProgress.setMax(maxProgress);
            }
            songProgress.setProgress(progress);
        }
        if (songProgress2 != null) {
            if (songProgress2.getMax() != maxProgress) {
                songProgress2.setMax(maxProgress);
            }
            songProgress2.setProgress(progress);
        }
    }

    private void setSongName(String name) {
        if (songName != null) {
            songName.setText(name);
        }
    }

    private void setShuffle(boolean state) {
        if (shuffle != null) {
            if (state) {
                shuffle.setImageAlpha(255);
            } else {
                shuffle.setImageAlpha(82);
            }
        }
    }

    private void setRepeat(int state) {
        if (repeat != null) {
            repeat.setImageResource(getRepeatResourceBlack(state));
            if (state != 0) {
                repeat.setImageAlpha(255);
            } else {
                repeat.setImageAlpha(82);
            }
        }
    }

    private void setPlayPause(boolean isPlaying) {
        if (playPause != null) {
            playPause.setIconResource(getPlayPauseResourceBlack(isPlaying));
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "nowPlayingStyle":
            case "visualizerBars":
            case "enableVisualizer":
            case "visualizerType":
            case "visualizerHeight":
            case "visualizerAnimationFrameRate":
                recreate();
                break;
        }
    }

    public void setAlbumArt(Bitmap albumArt) {
        if (this.albumArt != null) {
            if (executorService != null) {
                executorService.execute(() -> {
                    final Drawable roundedAlbumArt = getRoundedDrawable(getApplicationContext(), albumArt);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (NowPlayingActivity.this.albumArt != null) {
                            NowPlayingActivity.this.albumArt.setImageDrawable(roundedAlbumArt);
                        }
                    });
                });
            }
        }
    }

    public void setColors(int backgroundColor, int foregroundColor) {
        if (nowPlayingStyle == 0 || nowPlayingStyle == 1) {
            if (ContrastColor(getThemeWindowBackgroundColor(NowPlayingActivity.this)) != ContrastColor(backgroundColor)) {
                foregroundColor = backgroundColor;
            }
            backgroundColor = getThemeWindowBackgroundColor(NowPlayingActivity.this);
        }
        animateColors(backgroundColor);
        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(NowPlayingActivity.this, ContrastColor(backgroundColor) == Color.BLACK);
        }
        if (nowPlayingLyrics != null) {
            nowPlayingLyrics.setColors(backgroundColor, foregroundColor);
        }
        if (songProgress != null) {
            songProgress.setCircleStrokeWidth(dipToPixels(NowPlayingActivity.this, 4));
            songProgress.setCircleProgressColor(foregroundColor);
            songProgress.setCircleColor(adjustAlpha(foregroundColor, 0.2f));
            songProgress.setPointerStrokeWidth(dipToPixels(NowPlayingActivity.this, 6));
            songProgress.setPointerColor(foregroundColor);
            songProgress.setPointerHaloColor(foregroundColor);
        }
        int windowBackground = (nowPlayingStyle == 4 || nowPlayingStyle == 6) ? backgroundColor : nowPlayingStyle != 2 ? ThemeUtils.getThemeWindowBackgroundColor(NowPlayingActivity.this) : Color.BLACK;
        int tint;
        if (ContrastColor(windowBackground) == ContrastColor(backgroundColor)) {
            tint = foregroundColor;
        } else {
            tint = backgroundColor;
        }
        if (SymphonyApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
            if (waveVisualizer != null) {
                waveVisualizer.setColor(nowPlayingStyle != 1 ? tint : foregroundColor);
            }
            if (barVisualizer != null) {
                barVisualizer.setColor(nowPlayingStyle != 1 ? tint : foregroundColor);
            }
            if (circleLineVisualizer != null) {
                circleLineVisualizer.setColor(tint);
            }
        }
        setColorFilter(tint, songProgress2);
        setColorFilter(ContrastColor(windowBackground), shuffle, playPrevious, playNext, repeat, lyricsButton, openQueue, menu);
        setColorFilter(tint, favoriteButton);
        setTextColor(ContrastColor(windowBackground), lapsedTime2, totalDuration, songName);
        setTextColor(tint, artistNameAndAlbumName, lapsedTime);
        if (playPause != null) {
            playPause.setIconTint(ColorStateList.valueOf(backgroundColor));
            playPause.setBackgroundColor(foregroundColor);
            playPause.setRippleColor(ColorStateList.valueOf(backgroundColor));
        }
    }

    private void updateFavoriteButton() {
        if (executorService == null) {
            return;
        }
        executorService.execute(() -> {
            final boolean favorite = isCurrentSongFavorite(getApplicationContext());
            new Handler(Looper.getMainLooper()).post(() -> setFavorite(favorite));
        });
    }

    private void setFavorite(boolean favorite) {
        if (favoriteButton != null) {
            if (favorite) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_black_24dp);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
            }
        }
    }

    private void setLyrics() {
        if (nowPlayingLyrics == null) {
            return;
        }
        nowPlayingLyrics.setLyrics(getString(R.string.loading_lyrics));
        if (executorService == null) {
            return;
        }
        executorService.execute(() -> {
            try {
                final String lyricsString = getLyrics(SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().get(getSongPosition()).getPath(), getApplicationContext(), getCurrentSong().getId());
                new Handler(Looper.getMainLooper()).post(() -> nowPlayingLyrics.setLyrics(lyricsString));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showLyrics() {
        if (bottomSheetLyricsFragment == null) {
            bottomSheetLyricsFragment = new BottomSheetLyricsFragment();
            bottomSheetLyricsFragment.setCallback(nowPlayingLyrics -> {
                if (nowPlayingLyrics != null) {
                    this.nowPlayingLyrics = nowPlayingLyrics;
                    this.nowPlayingLyrics.setClickEventListener(this);
                    setLyrics();
                    AlbumArt albumArt = EventBus.getDefault().getStickyEvent(AlbumArt.class);
                    this.nowPlayingLyrics.setColors(albumArt.backgroundColor, albumArt.foregroundColor);
                }
            });
        }
        bottomSheetLyricsFragment.show(getSupportFragmentManager(), bottomSheetLyricsFragment.tag);
    }

    @Override
    public void onSearchClicked() {
        Song song = getCurrentSong();
        String track = getRealString(song.getName());
        String artist = getRealString(song.getArtist());
        try {
            String url = "https://www.google.com/search?q=" + track + URLEncoder.encode(" ", "UTF-8") + artist + URLEncoder.encode(" ", "UTF-8") + "lyrics";
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            try {
                builder.setToolbarColor(ThemeUtils.getThemePrimaryColor(NowPlayingActivity.this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(NowPlayingActivity.this, Uri.parse(url));
        } catch (Exception e) {
            postToast(R.string.error_label, getApplicationContext(), TOAST_ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public void onAddClicked() {
        showInputDialog(NowPlayingActivity.this,
                R.string.add_lyrics,
                R.string.type_lyrics,
                input -> {
                    Song song = getCurrentSong();
                    setLyricsInCache(getApplicationContext(), input, song.getId());
                    setLyrics();
                });
    }

    private void setUpRotation() {
        if (albumArt != null) {
            rotation = ObjectAnimator.ofFloat(albumArt, "rotation", 0, 359);
            rotation.setDuration(10000);
            rotation.setRepeatCount(ObjectAnimator.INFINITE);
            rotation.setRepeatMode(ObjectAnimator.RESTART);
            rotation.setInterpolator(new LinearInterpolator());

            rotation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    animation.setInterpolator(new LinearInterpolator());
                }
            });
        }
    }

    private void updateQueue(@NonNull SongListChanged songListChanged) {
        if (songListChanged.songs != null && nowPlayingBackground != null) {
            nowPlayingBackground.setSongs(songListChanged.songs);
            nowPlayingBackground.setPreviousPosition(SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition());
            nowPlayingBackground.scrollTo(SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition());
        }
    }

    public void scrollTo(final int position) {
        if (nowPlayingBackground != null) {
            nowPlayingBackground.scrollTo(position);
        }
    }

    private void animateColors(final int backgroundColor) {
        if (colorAnimation != null) {
            colorAnimation.end();
        }
        colorAnimation = new ValueAnimator();
        colorAnimation.setIntValues(previousBackgroundColor, backgroundColor);
        colorAnimation.setEvaluator(new ArgbEvaluator());
        colorAnimation.setDuration(500);
        int windowBackground = (nowPlayingStyle == 4 || nowPlayingStyle == 6) ? backgroundColor : getThemeWindowBackgroundColor(NowPlayingActivity.this);
        colorAnimation.addUpdateListener(valueAnimator -> {
            try {
                int bgColor = (int) (Integer) valueAnimator.getAnimatedValue();
                if (nowPlayingStyle != 2) {
                    if (container != null) {
                        GradientDrawable gradientDrawable = new GradientDrawable(
                                GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{bgColor, windowBackground}
                        );
                        gradientDrawable.setCornerRadius(0);
                        container.setBackground(gradientDrawable);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                colorAnimation.removeAllListeners();
                colorAnimation.removeAllUpdateListeners();
                previousBackgroundColor = backgroundColor;
            }
        });
        colorAnimation.start();
    }

    private void setUpVisualizer() {
        if (SymphonyApplication.getInstance().getPreferenceUtils().getEnableVisualizer()) {
            visualizerType = SymphonyApplication.getInstance().getPreferenceUtils().getVisualizerType();
            int visualizerSpeed = SymphonyApplication.getInstance().getPreferenceUtils().getVisualizerSpeed();
            if (visualizerType == 0) {
                if (circleLineVisualizer != null) {
                    circleLineVisualizer.setAnimationSpeed(calculateVisualizerAnimationSpeed(visualizerSpeed));
                }
                if (waveVisualizer != null) {
                    waveVisualizer.hide();
                }
                if (barVisualizer != null) {
                    barVisualizer.hide();
                }
            } else if (visualizerType == 1) {
                if (barVisualizer != null) {
                    barVisualizer.setAnimationSpeed(calculateVisualizerAnimationSpeed(visualizerSpeed));
                }
                if (waveVisualizer != null) {
                    waveVisualizer.hide();
                }
                if (circleLineVisualizer != null) {
                    circleLineVisualizer.setVisibility(View.INVISIBLE);
                }
            } else {
                if (waveVisualizer != null) {
                    waveVisualizer.setAnimationSpeed(calculateVisualizerAnimationSpeed(visualizerSpeed));
                }
                if (barVisualizer != null) {
                    barVisualizer.hide();
                }
                if (circleLineVisualizer != null) {
                    circleLineVisualizer.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (waveVisualizer != null) {
                waveVisualizer.setVisibility(View.INVISIBLE);
            }
            if (barVisualizer != null) {
                barVisualizer.setVisibility(View.INVISIBLE);
            }
            if (circleLineVisualizer != null) {
                circleLineVisualizer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onBackgroundScrolled(int position) {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition() != position) {
            playAtSongPosition(position);
        }
    }

    private AnimSpeed calculateVisualizerAnimationSpeed(int visualizerSpeed) {
        switch (visualizerSpeed) {
            case 0: {
                return AnimSpeed.FAST;
            }
            case 1: {
                return AnimSpeed.MEDIUM;
            }
            case 2: {
                return AnimSpeed.SLOW;
            }
            default: {
                return AnimSpeed.MEDIUM;
            }
        }
    }
}