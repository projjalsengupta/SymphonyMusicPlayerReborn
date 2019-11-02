package music.symphony.com.materialmusicv2.activities;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.adapters.MostPlayedAdapter;
import music.symphony.com.materialmusicv2.adapters.SongAdapter;
import music.symphony.com.materialmusicv2.adapters.SongDragAdapter;
import music.symphony.com.materialmusicv2.customviews.others.BottomPlaybackController;
import music.symphony.com.materialmusicv2.objects.MostPlayedList;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.misc.DragSortRecycler;
import music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setBackgroundColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.hasPlaybackStartedEvenOnce;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.shuffleList;
import static music.symphony.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeColorControlHighlight;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class GenrePlaylistActivity extends MusicPlayerActivity {

    private ArrayList<Song> songs;
    private ArrayList<Integer> playCountList;

    private SongAdapter songAdapter;
    private SongDragAdapter songDragAdapter;
    private MostPlayedAdapter mostPlayedAdapter;

    DragSortRecycler dragSortRecycler;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.genrePlaylistSongCount)
    TextView genrePlaylistSongCount;
    @BindView(R.id.genrePlaylistDetailsContainer)
    LinearLayout genrePlaylistDetailsContainer;
    @BindView(R.id.genrePlaylistName)
    TextView genrePlaylistName;
    @BindView(R.id.genrePlaylistSongsDuration)
    TextView genrePlaylistSongsDuration;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.bottomController)
    BottomPlaybackController bottomController;
    @BindView(R.id.add)
    MaterialButton add;
    @BindView(R.id.playAll)
    ExtendedFloatingActionButton playAll;
    @BindView(R.id.shuffleAll)
    MaterialButton shuffleAll;
    @BindView(R.id.addToQueue)
    MaterialButton addToQueue;
    @BindView(R.id.playlistImage)
    ImageView playlistImage;

    @OnClick({R.id.add, R.id.playAll, R.id.shuffleAll, R.id.addToQueue})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add: {
                Intent intent = new Intent(GenrePlaylistActivity.this, AddSongsToPlaylistActivity.class);
                intent.putExtra("ID", ID);
                startActivity(intent);
                break;
            }
            case R.id.shuffleAll: {
                shuffleList(songs);
                break;
            }
            case R.id.playAll: {
                playList(songs, 0);
                break;
            }
            case R.id.addToQueue: {
                addListToQueue(songs);
                break;
            }
        }
    }

    private long ID = -1;
    private int whatToDo = -1;
    private String title = null;

    int colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));
        setContentView(R.layout.activity_genre_playlist);

        ButterKnife.bind(this);

        loadAll();
    }

    public void loadAll() {
        colorPrimary = getThemePrimaryColor(GenrePlaylistActivity.this);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(GenrePlaylistActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        if (getIntent().getExtras() != null) {
            title = getIntent().getExtras().getString("TITLE");
            ID = getIntent().getExtras().getLong("ID");
            whatToDo = getIntent().getExtras().getInt("WHATTODO");
        }

        if (whatToDo != 5 && add != null) {
            add.setVisibility(View.GONE);
        }

        ToolbarUtils.setUpToolbar(
                toolbar,
                "",
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                ThemeUtils.getThemeAccentColor(GenrePlaylistActivity.this),
                GenrePlaylistActivity.this,
                this::onBackPressed,
                true
        );

        if (genrePlaylistName != null) {
            genrePlaylistName.setText(title);
        }

        int accentColor = ThemeUtils.getThemeAccentColor(GenrePlaylistActivity.this);
        int contrastColorAccent = ContrastColor(accentColor);

        setBackgroundColorFilter(getThemeWindowBackgroundColor(GenrePlaylistActivity.this), genrePlaylistDetailsContainer);
        setColorFilter(contrastColorAccent, playAll);

        if (shuffleAll != null) {
            shuffleAll.setTextColor(contrastColorAccent);
            shuffleAll.setIconTint(ColorStateList.valueOf(contrastColorAccent));
        }

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(GenrePlaylistActivity.this));

        getSongs();
        setPlaylistImage();

        dragSortRecycler = new DragSortRecycler(getThemeColorControlHighlight(GenrePlaylistActivity.this));
        recyclerView.removeItemDecoration(dragSortRecycler);
        recyclerView.removeOnItemTouchListener(dragSortRecycler);
        recyclerView.removeOnScrollListener(dragSortRecycler.getScrollListener());
        if (!songs.isEmpty()) {
            if (whatToDo == 5) {
                dragSortRecycler.setViewHandleId(R.id.dragHandle);
                dragSortRecycler.setOnItemMovedListener((from, to) -> {
                    if (from != to) {
                        moveItem(from, to);
                    }
                });
                recyclerView.addItemDecoration(dragSortRecycler);
                recyclerView.addOnItemTouchListener(dragSortRecycler);
                recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
                songDragAdapter = new SongDragAdapter(songs, GenrePlaylistActivity.this, ID);
                recyclerView.setAdapter(songDragAdapter);
            } else if (whatToDo == 3) {
                mostPlayedAdapter = new MostPlayedAdapter(songs, playCountList, GenrePlaylistActivity.this);
                recyclerView.setAdapter(mostPlayedAdapter);
            } else {
                songAdapter = new SongAdapter(songs, GenrePlaylistActivity.this, false);
                recyclerView.setAdapter(songAdapter);
            }
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(GenrePlaylistActivity.this, R.anim.layout_animation_fall_down);
            recyclerView.setLayoutAnimation(animation);
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

        loadBottomController();

        connectReceiver();
    }

    private void moveItem(int from, int to) {
        boolean success = false;
        LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        if (linearLayoutManager != null) {
            int firstPos = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
            int offsetTop = 0;
            if (firstPos >= 0) {
                View firstView = linearLayoutManager.findViewByPosition(firstPos);
                if (firstView != null) {
                    offsetTop = linearLayoutManager.getDecoratedTop(firstView) - linearLayoutManager.getTopDecorationHeight(firstView);
                }
            }
            success = songDragAdapter.moveItem(from, to);
            if (firstPos >= 0) {
                linearLayoutManager.scrollToPositionWithOffset(firstPos, offsetTop);
            }
        }
        if (to == 0) {
            recyclerView.scrollToPosition(0);
        }
        if (success) {
            songs.add(to, songs.remove(from));
        }
    }

    @Override
    public void onDestroy() {
        recyclerView.setAdapter(null);
        songAdapter = null;
        songDragAdapter = null;
        mostPlayedAdapter = null;
        try {
            unregisterReceiver(genrePlaylistActivityBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void setPlaylistImage() {
        if (playlistImage == null) {
            return;
        }
        switch (whatToDo) {
            case 0: {
                playlistImage.setVisibility(View.INVISIBLE);
                break;
            }
            case 1: {
                playlistImage.setImageResource(R.drawable.ic_queue_black_24dp);
                playlistImage.setVisibility(View.VISIBLE);
                setColorFilter(getThemeTextColorPrimary(GenrePlaylistActivity.this), playlistImage);
                break;
            }
            case 2: {
                playlistImage.setImageResource(R.drawable.ic_history_black_24dp);
                playlistImage.setVisibility(View.VISIBLE);
                setColorFilter(getThemeTextColorPrimary(GenrePlaylistActivity.this), playlistImage);
                break;
            }
            case 3: {
                playlistImage.setImageResource(R.drawable.ic_most_played_black_24dp);
                playlistImage.setVisibility(View.VISIBLE);
                setColorFilter(getThemeTextColorPrimary(GenrePlaylistActivity.this), playlistImage);
                break;
            }
            case 5: {
                playlistImage.setImageResource(R.drawable.ic_favorite_black_24dp);
                playlistImage.setVisibility(View.VISIBLE);
                setColorFilter(ContextCompat.getColor(GenrePlaylistActivity.this, R.color.md_red_600), playlistImage);
                break;
            }
        }
    }

    private void getSongs() {
        ContentResolver contentResolver = getContentResolver();
        if (contentResolver != null) {
            switch (whatToDo) {
                case 0: {
                    songs = QueryUtils.getAllSongsFromGenre(getContentResolver(), MediaStore.Audio.Media.TITLE, ID);
                    break;
                }
                case 1: {
                    songs = QueryUtils.getAllSongs(getContentResolver(), MediaStore.Audio.Media.DATE_ADDED);
                    Collections.reverse(songs);
                    if (songs.size() > 100) {
                        songs = new ArrayList<>(songs.subList(0, 100));
                    }
                    break;
                }
                case 2: {
                    songs = QueryUtils.getLastPlayedSongs(GenrePlaylistActivity.this);
                    songs = QueryUtils.removeNonExistentSongs(songs);
                    break;
                }
                case 3: {
                    songs = QueryUtils.getMostPlayedSongs(GenrePlaylistActivity.this);
                    playCountList = QueryUtils.getMostPlayedSongPlayCountList(GenrePlaylistActivity.this);
                    MostPlayedList mostPlayedList = QueryUtils.removeNonExistentSongs(songs, playCountList);
                    songs = mostPlayedList.songs;
                    playCountList = mostPlayedList.playCountList;
                    break;
                }
                case 5: {
                    songs = QueryUtils.getAllSongsFromPlaylist(getContentResolver(), ID);
                    break;
                }
            }

            int totalDuration = 0;
            String durationString;
            for (Song song : songs) {
                totalDuration += song.getDuration();
            }
            durationString = covertMilisToTimeString(totalDuration);

            if (genrePlaylistSongCount != null) {
                genrePlaylistSongCount.setText(String.format(getString(R.string.number_of_songs_placeholder), songs.size()));
            }

            if (genrePlaylistSongsDuration != null) {
                genrePlaylistSongsDuration.setText(durationString);
            }

        } else {
            postToast(R.string.error_label, GenrePlaylistActivity.this, TOAST_ERROR);
        }
    }

    private void loadBottomController() {
        if (bottomController == null) {
            return;
        }

        bottomController.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(GenrePlaylistActivity.this));
        bottomController.setProgressColor(ContrastColor(ThemeUtils.getThemeWindowBackgroundColor(GenrePlaylistActivity.this)), ThemeUtils.getThemeAccentColor(GenrePlaylistActivity.this));

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
            Intent nowPlayingIntent = new Intent(GenrePlaylistActivity.this, NowPlayingActivity.class);
            startActivity(nowPlayingIntent);
        } else {
            postToast(R.string.no_song_is_playing, GenrePlaylistActivity.this, TOAST_INFO);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    public class GenrePlaylistActivityBroadcastReceiver extends BroadcastReceiver {

        public GenrePlaylistActivityBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (RELOAD_LIBRARY_INTENT.equals(intent.getAction())) {
                    loadAll();
                }
            }
        }
    }

    private GenrePlaylistActivityBroadcastReceiver genrePlaylistActivityBroadcastReceiver = new GenrePlaylistActivityBroadcastReceiver();

    private void connectReceiver() {
        if (genrePlaylistActivityBroadcastReceiver == null) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RELOAD_LIBRARY_INTENT);
        registerReceiver(genrePlaylistActivityBroadcastReceiver, intentFilter);
    }

    private void openPlayingQueue() {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongs() != null && SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(GenrePlaylistActivity.this, QueueActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, GenrePlaylistActivity.this, TOAST_INFO);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong) {
        if (whatToDo == 3 && recyclerView != null && mostPlayedAdapter != null) {
            Song song = SymphonyApplication.getInstance().getPlayingQueueManager().getCurrentSong();
            int position = songs.indexOf(song);
            if (position != -1) {
                playCountList.set(position, playCountList.get(position) + 1);
                while (position > 0 && playCountList.get(position) > playCountList.get(position - 1)) {
                    songs.add(position - 1, songs.remove(position));
                    playCountList.add(position - 1, playCountList.remove(position));
                    position--;
                }
                if (position == 0) {
                    recyclerView.scrollToPosition(0);
                }
            } else {
                songs.add(song);
                playCountList.add(1);
            }
            mostPlayedAdapter.changeSongs(songs, playCountList);
        }
    }
}