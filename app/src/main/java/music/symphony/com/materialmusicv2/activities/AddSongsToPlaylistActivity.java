package music.symphony.com.materialmusicv2.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.adapters.AddSongsToPlaylistAdapter;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.addListToPlaylist;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class AddSongsToPlaylistActivity extends MusicPlayerActivity {

    private ArrayList<Song> songs = new ArrayList<>();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.done)
    FloatingActionButton done;

    @OnClick({R.id.done})
    public void onClick(View view) {
        if (view.getId() == R.id.done) {
            if (addSongsToPlaylistAdapter != null) {
                ArrayList<Song> checkedSongs = new ArrayList<>(addSongsToPlaylistAdapter.getCheckedSongs());
                if (checkedSongs.size() > 0) {
                    AsyncTask.execute(() -> {
                        addListToPlaylist(getContentResolver(), checkedSongs, ID);
                        new Handler(Looper.getMainLooper()).post(() -> {
                            postToast(String.format(getString(R.string.add_to_playlist_toast), checkedSongs.size()), AddSongsToPlaylistActivity.this, TOAST_SUCCESS);
                            Intent intent = new Intent(RELOAD_LIBRARY_INTENT);
                            sendBroadcast(intent);
                            finish();
                        });
                    });
                }
            }
        }
    }

    private long ID = -1;

    int colorPrimary;
    int colorAccent;

    AddSongsToPlaylistAdapter addSongsToPlaylistAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));
        setContentView(R.layout.activity_add_songs_to_playlist);

        ButterKnife.bind(this);

        colorAccent = getThemeAccentColor(AddSongsToPlaylistActivity.this);
        colorPrimary = getThemePrimaryColor(AddSongsToPlaylistActivity.this);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(AddSongsToPlaylistActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        if (getIntent().getExtras() != null) {
            ID = getIntent().getExtras().getLong("ID");
        }

        if (done != null) {
            done.setColorFilter(ContrastColor(colorAccent), PorterDuff.Mode.SRC_ATOP);
        }

        ToolbarUtils.setUpToolbar(toolbar,
                getString(R.string.add_songs_to_playlist),
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                colorPrimary,
                AddSongsToPlaylistActivity.this,
                this::onBackPressed
        );

        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddSongsToPlaylistActivity.this));

        loadSongs();
    }

    public void loadSongs() {
        songs.clear();
        songs = new ArrayList<>();
        getSongs();
        if (!songs.isEmpty()) {
            addSongsToPlaylistAdapter = new AddSongsToPlaylistAdapter(songs, AddSongsToPlaylistActivity.this);
            recyclerView.setAdapter(addSongsToPlaylistAdapter);
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(AddSongsToPlaylistActivity.this, R.anim.layout_animation_fall_down);
            recyclerView.setLayoutAnimation(animation);
        }
    }

    @Override
    public void onDestroy() {
        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    private void getSongs() {
        ContentResolver contentResolver = getContentResolver();
        if (contentResolver != null) {
            ArrayList<Song> playlistSongs = songs = QueryUtils.getAllSongsFromPlaylist(getContentResolver(), ID);
            songs = QueryUtils.getAllSongs(contentResolver, MediaStore.Audio.Media.TITLE);
            songs.removeAll(playlistSongs);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}