package music.symphony.com.materialmusicv2.activities;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.adapters.AlbumSongAdapter;
import music.symphony.com.materialmusicv2.customviews.others.BottomPlaybackController;
import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.misc.Statics;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setBackgroundColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setBackgroundColorFilter;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.hasPlaybackStartedEvenOnce;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.shuffleList;
import static music.symphony.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getAlbumsOfArtist;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getSongsOfAlbum;

public class AlbumActivity extends MusicPlayerActivity {

    @BindView(R.id.gradient)
    View gradient;
    @BindView(R.id.collapsingToolbarLayout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.albumArt)
    ImageView albumArt;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.shuffleAll)
    MaterialButton shuffleAll;
    @BindView(R.id.addToQueue)
    MaterialButton addToQueue;
    @BindView(R.id.addToPlaylist)
    MaterialButton addToPlaylist;
    @BindView(R.id.playAll)
    ExtendedFloatingActionButton playAll;
    @BindView(R.id.albumActivityRoot)
    CoordinatorLayout albumActivityRoot;
    @BindView(R.id.bottomController)
    BottomPlaybackController bottomController;
    @BindView(R.id.artistName)
    TextView artistName;
    @BindView(R.id.albumSongCount)
    TextView albumSongCount;
    @BindView(R.id.albumSongsDuration)
    TextView albumSongsDuration;
    @BindView(R.id.albumYear)
    TextView albumYear;
    @BindView(R.id.albumDetailsContainer)
    RelativeLayout albumDetailsContainer;
    @BindView(R.id.albumName)
    TextView albumName;

    private ArrayList<Song> songs;

    private long albumID;
    private String albumNameString;
    private String artistNameString;
    private String albumYearString;

    private int accentColorFromAlbumArt;

    @OnClick({R.id.playAll, R.id.shuffleAll, R.id.addToQueue, R.id.addToPlaylist, R.id.artistName})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.artistName: {
                try {
                    Intent intent = new Intent(AlbumActivity.this, ArtistActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ARTIST_TITLE", artistNameString);
                    bundle.putLong("ARTIST_ID", songs.get(0).getArtistId());
                    intent.putExtras(bundle);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.playAll: {
                playList(songs, 0);
                break;
            }
            case R.id.shuffleAll: {
                shuffleList(songs);
                break;
            }
            case R.id.addToQueue: {
                addListToQueue(songs);
                break;
            }
            case R.id.addToPlaylist: {
                DialogUtils.showAddToPlaylistDialog(AlbumActivity.this, songs);
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(ThemeUtils.getTheme(this));
        setContentView(R.layout.activity_album);
        ButterKnife.bind(this);
        obtainProvidedData();
        setColors(ThemeUtils.getThemeAccentColor(AlbumActivity.this));
        loadBottomController();
        setUp();
        connectReceiver();
    }

    private void obtainProvidedData() {
        if (getIntent().getExtras() != null) {
            albumID = getIntent().getExtras().getLong("ALBUM_ID");
            albumNameString = getIntent().getExtras().getString("ALBUM_NAME");
            artistNameString = getIntent().getExtras().getString("ARTIST_NAME");
            albumYearString = String.valueOf(getIntent().getExtras().getInt("ALBUM_YEAR"));
            if (albumYearString.equals("0")) {
                albumYearString = "-";
            }
        }
    }

    public void setColors(int color) {
        int windowBackgroundColor = ThemeUtils.getThemeWindowBackgroundColor(AlbumActivity.this);

        accentColorFromAlbumArt = color;

        int contrastColor = ContrastColor(color);
        int contrastWindowBackground = ContrastColor(windowBackgroundColor);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(AlbumActivity.this, contrastColor == Color.BLACK);
        }

        setBackgroundColorFilter(windowBackgroundColor, gradient);
        setBackgroundColor(new int[]{color}, new View[]{albumActivityRoot});

        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setContentScrimColor(color);
        }

        if (shuffleAll != null) {
            shuffleAll.setBackgroundColor(color);
            shuffleAll.setTextColor(contrastColor);
            shuffleAll.setStrokeColor(ColorStateList.valueOf(contrastColor));
            shuffleAll.setIconTint(ColorStateList.valueOf(contrastColor));
            shuffleAll.setRippleColor(ColorStateList.valueOf(contrastColor));
        }
        if (addToPlaylist != null) {
            addToPlaylist.setStrokeColor(ColorStateList.valueOf(contrastWindowBackground));
            addToPlaylist.setTextColor(contrastWindowBackground);
            addToPlaylist.setIconTint(ColorStateList.valueOf(contrastWindowBackground));
            addToPlaylist.setRippleColor(ColorStateList.valueOf(contrastWindowBackground));
        }
        if (addToQueue != null) {
            addToQueue.setStrokeColor(ColorStateList.valueOf(contrastWindowBackground));
            addToQueue.setTextColor(contrastWindowBackground);
            addToQueue.setIconTint(ColorStateList.valueOf(contrastWindowBackground));
            addToQueue.setRippleColor(ColorStateList.valueOf(contrastWindowBackground));
        }
        if (playAll != null) {
            playAll.setTextColor(contrastColor);
            playAll.setIconTint(ColorStateList.valueOf(contrastColor));
            playAll.setBackgroundColor(color);
            playAll.setRippleColor(ColorStateList.valueOf(ContrastColor(color)));
        }
        if (bottomController != null) {
            bottomController.setBackgroundColor(windowBackgroundColor);
            bottomController.setProgressColor(contrastWindowBackground, color);
        }

        ToolbarUtils.setUpToolbar(
                toolbar,
                "",
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                color,
                AlbumActivity.this,
                this::onBackPressed,
                true
        );

        if (recyclerView != null && recyclerView.getAdapter() != null) {
            ((AlbumSongAdapter) recyclerView.getAdapter()).setAccentColor(color);
        }
    }

    public void loadSongs() {
        songs = getSongsOfAlbum(albumID, albumNameString, artistNameString, getContentResolver());
        Collections.sort(songs, (song, t1) -> Integer.compare(song.getTrackNumber(), t1.getTrackNumber()));
        ArrayList<Album> artistAlbums = getAlbumsOfArtist(getContentResolver(), artistNameString);
        for (Album album : artistAlbums) {
            if (album.getId() == albumID) {
                artistAlbums.remove(album);
                break;
            }
        }
        if (songs == null) {
            return;
        }
        long totalDuration = 0;

        for (Song song : songs) {
            if (song != null) {
                totalDuration += song.getDuration();
            }
        }
        final String durationString = covertMilisToTimeString(totalDuration);

        AlbumSongAdapter albumSongAdapter = new AlbumSongAdapter(songs, artistAlbums, accentColorFromAlbumArt, AlbumActivity.this);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(AlbumActivity.this));
            recyclerView.setAdapter(albumSongAdapter);
        }
        if (artistName != null) {
            artistName.setText(artistNameString);
        }
        if (albumSongCount != null) {
            albumSongCount.setText(String.format(getString(R.string.number_of_songs_placeholder), songs.size()));
        }
        if (albumSongsDuration != null) {
            albumSongsDuration.setText(durationString);
        }
        if (albumYear != null) {
            albumYear.setText(albumYearString);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (songs != null) {
                songs.clear();
                songs = null;
            }
            if (recyclerView != null) {
                recyclerView.setAdapter(null);
            }
            unregisterReceiver(albumActivityBroadcastReceiver);
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUp() {
        loadSongs();
        accentColorFromAlbumArt = ThemeUtils.getThemeAccentColor(AlbumActivity.this);
        try {
            int backgroundPixels = getResources().getDisplayMetrics().widthPixels;
            GlideApp.with(this)
                    .as(PaletteBitmap.class)
                    .load(ContentUris.withAppendedId(Statics.artworkUri, albumID))
                    .apply(new RequestOptions()
                            .override(backgroundPixels, backgroundPixels)
                            .error(R.drawable.ic_blank_album_art)
                    )
                    .centerCrop()
                    .into(new ImageViewTarget<PaletteBitmap>(albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            try {
                                if (SymphonyApplication.getInstance().getPreferenceUtils().getColorizeElementsAccordingToAlbumArt()) {
                                    setColors(ContrastColor(resource.foregroundColor) == ContrastColor(ThemeUtils.getThemeWindowBackgroundColor(AlbumActivity.this)) ? resource.backgroundColor : resource.foregroundColor);
                                }
                                if (resource != null && resource.bitmap != null) {
                                    albumArt.setImageBitmap(resource.bitmap);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (albumArt != null) {
            supportPostponeEnterTransition();
            albumArt.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            albumArt.getViewTreeObserver().removeOnPreDrawListener(this);
                            supportStartPostponedEnterTransition();
                            return true;
                        }
                    }
            );
        }
        if (albumName != null) {
            albumName.setText(albumNameString);
        }
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                        if (playAll != null) {
                            playAll.shrink();
                        }
                    }
                    if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                        if (playAll != null) {
                            playAll.extend();
                        }
                    }
                }
            });
        }
    }

    private void loadBottomController() {
        if (bottomController == null) {
            return;
        }
        bottomController.setOnClickEventDetectedListener(new BottomPlaybackController.OnClickEventDetectedListener() {

            @Override
            public void onPlayPreviousClicked() {
                playPrevious();
            }

            @Override
            public void onPlayPauseClicked() {
                pauseOrResumePlayer();
            }

            @Override
            public void onPlayNextClicked() {
                playNext();
            }

            @Override
            public void onSongNameClicked() {
                openNowPlayingActivity();
            }

            @Override
            public void onOpenPlayingQueueClicked() {
                openPlayingQueue();
            }
        });
    }

    private void openNowPlayingActivity() {
        if (hasPlaybackStartedEvenOnce()) {
            Intent nowPlayingIntent = new Intent(AlbumActivity.this, NowPlayingActivity.class);
            startActivity(nowPlayingIntent);
        } else {
            postToast(R.string.no_song_is_playing, AlbumActivity.this, TOAST_INFO);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public class AlbumActivityBroadcastReceiver extends BroadcastReceiver {

        public AlbumActivityBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (RELOAD_LIBRARY_INTENT.equals(intent.getAction())) {
                    setUp();
                }
            }
        }
    }

    private AlbumActivityBroadcastReceiver albumActivityBroadcastReceiver = new AlbumActivityBroadcastReceiver();

    private void connectReceiver() {
        if (albumActivityBroadcastReceiver == null) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RELOAD_LIBRARY_INTENT);
        registerReceiver(albumActivityBroadcastReceiver, intentFilter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        AlbumActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openPlayingQueue() {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongs() != null && SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(AlbumActivity.this, QueueActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, AlbumActivity.this, TOAST_INFO);
        }
    }
}