package music.symphony.com.materialmusicv2.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomNavigationDrawerFragment;
import music.symphony.com.materialmusicv2.customviews.library.LibraryController;
import music.symphony.com.materialmusicv2.customviews.library.LibraryToolbar;
import music.symphony.com.materialmusicv2.customviews.others.RoundedSquareImageView;
import music.symphony.com.materialmusicv2.fragments.FragmentFolders;
import music.symphony.com.materialmusicv2.fragments.FragmentLibrary;
import music.symphony.com.materialmusicv2.objects.events.AlbumArt;
import music.symphony.com.materialmusicv2.objects.events.CurrentPlayingSong;
import music.symphony.com.materialmusicv2.objects.events.PlaybackPosition;
import music.symphony.com.materialmusicv2.objects.events.PlaybackState;
import music.symphony.com.materialmusicv2.objects.events.Refresh;
import music.symphony.com.materialmusicv2.objects.events.RefreshAdapter;
import music.symphony.com.materialmusicv2.objects.events.RefreshGrid;
import music.symphony.com.materialmusicv2.objects.events.RepeatState;
import music.symphony.com.materialmusicv2.objects.events.ShuffleState;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.PlaySongAtStart;
import music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.RealPathUtil;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.customviews.others.RoundedSquareImageView.CORNER_ALL;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.changeRepeat;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.changeShuffle;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.setTimer;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.shuffleList;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeWindowBackgroundColor;

public class MainActivity extends MusicPlayerActivity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnTouchListener {

    int currentFragment = 0;

    @Nullable
    @BindView(R.id.libraryToolbar)
    LibraryToolbar libraryToolbar;
    @BindView(R.id.libraryController)
    LibraryController libraryController;
    @BindView(R.id.elementsContainer)
    FrameLayout elementsContainer;

    private BottomNavigationDrawerFragment bottomNavigationDrawerFragment;

    private TextView songName;
    private TextView artistName;
    private RoundedSquareImageView albumArt;

    private FragmentLibrary fragmentLibrary;
    private FragmentFolders fragmentFolders;

    private float dp;
    private float dX;
    private float dY;
    private int lastAction;
    private float idealXPosition;
    private boolean isLibraryControllerHidden = false;

    private boolean mainScreenStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SymphonyApplication.getInstance().getPreferenceUtils().getIsIntroShown()) {
            Intent intent = new Intent(MainActivity.this, SymphonyIntroActivity.class);
            startActivity(intent);
        }

        setTheme(ThemeUtils.getTheme(this));

        mainScreenStyle = SymphonyApplication.getInstance().getPreferenceUtils().getMainScreenStyle();

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        int colorPrimary = getThemePrimaryColor(this);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(MainActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        if (libraryToolbar != null) {
            libraryToolbar.setTitle(getString(R.string.search_your_library));
            libraryToolbar.setHamburgerMenuClick(this::openNavDrawer);
            libraryToolbar.setMenuClick(this::toolbarMenuClick);
            libraryToolbar.setTitleClick(() -> {
                if (currentFragment == 0) {
                    openSearchActivity();
                }
            });
            libraryToolbar.setBackgroundColor(getThemeWindowBackgroundColor(MainActivity.this));
        }

        setFragment(SymphonyApplication.getInstance().getPreferenceUtils().getLastOpenedFragment());

        setUpLibraryController();

        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        connectReceiver();

        handleIntent(getIntent());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    private void setFragment(int index) {
        if (index == 0 && fragmentLibrary != null) {
            return;
        }
        if (index == 1 && fragmentFolders != null) {
            return;
        }
        currentFragment = index;
        Fragment fragment = null;
        if (index == 0) {
            fragmentLibrary = new FragmentLibrary();
            fragment = fragmentLibrary;
            fragmentFolders = null;
        } else if (index == 1) {
            fragmentFolders = new FragmentFolders();
            fragment = fragmentFolders;
            fragmentLibrary = null;
        }

        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.elementsContainer, fragment);
            fragmentTransaction.commit();
        }

        if (libraryToolbar != null) {
            if (index == 0) {
                libraryToolbar.setTitle(getString(R.string.search_your_library));
            } else if (index == 1) {
                libraryToolbar.setTitle(getString(R.string.folders_title));
            }
        }

        SymphonyApplication.getInstance().getPreferenceUtils().setLastOpenedFragment(currentFragment);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpLibraryController() {
        if (libraryController == null) {
            return;
        }

        if (mainScreenStyle) {
            libraryController.setVisibility(View.GONE);
            return;
        }

        libraryController.setOnClickEventDetectedListener(new LibraryController.OnClickEventDetectedListener() {
            @Override
            public void onShuffleClicked() {
                changeShuffle();
                resetClipping();
            }

            @Override
            public void onPlayPreviousClicked() {
                playPrevious();
                resetClipping();
            }

            @Override
            public void onPlayPauseClicked() {
                pauseOrResumePlayer();
                resetClipping();
            }

            @Override
            public void onPlayNextClicked() {
                playNext();
                resetClipping();
            }

            @Override
            public void onRepeatClicked() {
                changeRepeat();
                resetClipping();
            }

            @Override
            public void onBodyClicked() {
                openNowPlaying();
            }
        });

        if (!mainScreenStyle) {
            float width = getResources().getDisplayMetrics().widthPixels;
            dp = getResources().getDisplayMetrics().density;
            idealXPosition = width - 80 * dp;

            if (clipHandler != null) {
                clipHandler.postDelayed(clipRunnable, 2500);
            }
        }

        libraryController.setOnTouchListener(this);
    }

    private void openNowPlaying() {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongs() != null && SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getApplicationContext(), TOAST_INFO);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (mainScreenStyle) {
            return;
        }

        if (!isLibraryControllerHidden) {
            if (clipHandler != null) {
                clipHandler.postDelayed(clipRunnable, 2500);
            }
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
    public void onBackPressed() {
        if (currentFragment == 1 && fragmentFolders != null) {
            if (!fragmentFolders.onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(AlbumArt albumArt) {
        if (albumArt == null || albumArt.albumArt == null) {
            if (this.albumArt != null) {
                this.albumArt.setVisibility(View.GONE);
            }
            return;
        }
        if (this.albumArt != null) {
            this.albumArt.setVisibility(View.VISIBLE);
            if (SymphonyApplication.getInstance().getPreferenceUtils().getImageType() == 0) {
                this.albumArt.setImageDrawable(BitmapUtils.getRoundedDrawable(getApplicationContext(), albumArt.albumArt));
            } else if (SymphonyApplication.getInstance().getPreferenceUtils().getImageType() == 2) {
                this.albumArt.setCornerRadius(24);
                this.albumArt.setRoundedCorners(CORNER_ALL);
                this.albumArt.setImageBitmap(albumArt.albumArt);
            } else {
                this.albumArt.setImageBitmap(albumArt.albumArt);
            }
        }
        if (libraryController != null) {
            libraryController.onBitmapChanged(albumArt.albumArt);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackState playbackState) {
        if (libraryController != null) {
            libraryController.setPlayPause(playbackState.state);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(RepeatState repeatState) {
        if (libraryController != null) {
            libraryController.setRepeat(repeatState.state);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(ShuffleState shuffleState) {
        if (libraryController != null) {
            libraryController.setShuffle(shuffleState.state);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(PlaybackPosition playbackPosition) {
        if (libraryController != null) {
            libraryController.setPlaybackPosition(playbackPosition.position, playbackPosition.duration);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(CurrentPlayingSong currentPlayingSong) {
        if (currentPlayingSong == null) {
            if (this.songName != null) {
                this.songName.setVisibility(View.GONE);
            }
            if (this.artistName != null) {
                this.artistName.setVisibility(View.GONE);
            }
            return;
        }
        if (this.songName != null) {
            this.songName.setVisibility(View.VISIBLE);
            this.songName.setText(currentPlayingSong.songName);
        }
        if (this.artistName != null) {
            this.artistName.setVisibility(View.VISIBLE);
            this.artistName.setText(currentPlayingSong.artistName);
        }
        if (libraryController != null) {
            libraryController.setSongName(currentPlayingSong.songName);
        }
    }

    public void openNavDrawer() {
        bottomNavigationDrawerFragment = new BottomNavigationDrawerFragment();
        bottomNavigationDrawerFragment.setCallback(this::setUpNavigationView);
        bottomNavigationDrawerFragment.show(getSupportFragmentManager(), bottomNavigationDrawerFragment.tag);
    }

    private void setUpNavigationView(NavigationView navigationView) {
        if (navigationView == null) {
            return;
        }

        View headerView = navigationView.getHeaderView(0);
        songName = headerView.findViewById(R.id.songNameHeader);
        artistName = headerView.findViewById(R.id.artistNameHeader);
        albumArt = headerView.findViewById(R.id.albumArtHeader);

        int textColorPrimary = ThemeUtils.getThemeTextColorPrimary(MainActivity.this);
        songName.setTextColor(textColorPrimary);
        artistName.setTextColor(textColorPrimary);

        int[][] state = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{android.R.attr.state_enabled},
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_pressed}
        };

        int[] color = new int[]{
                getThemeAccentColor(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this),
                getThemeTextColorPrimary(MainActivity.this)
        };

        ColorStateList colorStateList = new ColorStateList(state, color);

        navigationView.setItemIconTintList(colorStateList);
        navigationView.setItemTextColor(colorStateList);

        navigationView.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(MainActivity.this));

        navigationView.getMenu().getItem(currentFragment).setChecked(true);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (bottomNavigationDrawerFragment != null) {
                bottomNavigationDrawerFragment.dismiss();
            }

            new Handler().postDelayed(() -> {
                switch (item.getItemId()) {
                    case R.id.nav_library: {
                        setFragment(0);
                        item.setChecked(true);
                        break;
                    }
                    case R.id.nav_folder: {
                        setFragment(1);
                        item.setChecked(true);
                        break;
                    }
                    case R.id.nav_settings: {
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_equalizer: {
                        Intent intent = new Intent(MainActivity.this, EqualizerActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_sleep_timer: {
                        DialogUtils.showSeekbarInputDialog(MainActivity.this,
                                R.string.sleep_timer,
                                R.string.sleep_timer_hint,
                                1,
                                60,
                                input -> {
                                    try {
                                        setTimer((Long.parseLong(input)) * 60 * 1000);
                                        postToast(R.string.sleep_timer_set, getApplicationContext(), TOAST_SUCCESS);
                                    } catch (Exception e) {
                                        postToast(R.string.invalid_input, getApplicationContext(), TOAST_ERROR);
                                    }
                                }
                        );
                        break;
                    }
                    case R.id.nav_blacklist: {
                        Intent intent = new Intent(MainActivity.this, BlacklistActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_about: {
                        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.nav_share: {
                        final String appPackageName = getPackageName();
                        String shareBody = "Download Symphony Music Player Reborn : https://play.google.com/store/apps/details?id=" + appPackageName;
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)));
                        break;
                    }
                    case R.id.nav_rate: {
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        break;
                    }
                }
            }, 350);
            return true;
        });


        onEvent(EventBus.getDefault().getStickyEvent(AlbumArt.class));
        onEvent(EventBus.getDefault().getStickyEvent(CurrentPlayingSong.class));
    }

    private ReloadLibraryBroadcastReceiver reloadLibraryBroadcastReceiver = new ReloadLibraryBroadcastReceiver();

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "language":
            case "theme":
            case "imageType":
            case "accentColor":
            case "donated":
            case "mainScreenStyle":
            case "defaultPage":
            case "isIntroEnabled":
            case "colorizeElementsAccordingToAlbumArt":
            case "tabTitlesMode":
            case "songItemStyle":
            case "genreItemStyle":
            case "playlistItemStyle":
            case "useCircularImage":
                recreate();
                break;
            case "albumGrid":
            case "artistGrid":
                if (currentFragment == 0 && fragmentLibrary != null) {
                    fragmentLibrary.reload();
                }
                break;
        }
    }

    private Handler clipHandler = new Handler();

    private Runnable clipRunnable = () -> {
        if (libraryController == null) {
            return;
        }

        float dx = libraryController.getLeft();

        ObjectAnimator animation = ObjectAnimator.ofFloat(libraryController, "translationX", idealXPosition - dx);
        animation.setDuration(500);
        animation.start();

        isLibraryControllerHidden = true;
    };

    private float startX = -1;
    private float startY = -1;

    private long startTime = -1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(@NonNull View view, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                startX = motionEvent.getRawX();
                startY = motionEvent.getRawY();

                dX = view.getX() - motionEvent.getRawX();
                dY = view.getY() - motionEvent.getRawY();

                startTime = System.currentTimeMillis();

                lastAction = MotionEvent.ACTION_DOWN;
                if (clipHandler != null) {
                    clipHandler.removeCallbacks(clipRunnable);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                isLibraryControllerHidden = false;

                float libraryControllerHeight = view.getHeight();
                float bottom = elementsContainer.getBottom();

                if (!mainScreenStyle) {
                    view.setY(Math.max(0, Math.min(motionEvent.getRawY() + dY, bottom - libraryControllerHeight - 72 * dp)));
                    view.setX(Math.max(0, motionEvent.getRawX() + dX));
                }

                lastAction = MotionEvent.ACTION_MOVE;
                break;
            }

            case MotionEvent.ACTION_UP: {
                float endX = motionEvent.getRawX();
                float endY = motionEvent.getRawY();

                if (lastAction == MotionEvent.ACTION_DOWN || (isAClick(startX, endX, startY, endY) && (System.currentTimeMillis() - startTime <= 100))) {
                    if (Math.abs(idealXPosition - libraryController.getX()) < 100) {
                        ObjectAnimator animation = ObjectAnimator.ofFloat(libraryController, "translationX", 0);
                        animation.setDuration(500);
                        animation.start();
                        isLibraryControllerHidden = false;
                        if (clipHandler != null) {
                            clipHandler.postDelayed(clipRunnable, 2500);
                        }
                    } else {
                        openNowPlaying();
                    }
                } else {
                    if (clipHandler != null) {
                        clipHandler.postDelayed(clipRunnable, 2500);
                    }
                }
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    private class ReloadLibraryBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (RELOAD_LIBRARY_INTENT.equals(intent.getAction())) {
                    if (fragmentLibrary != null) {
                        fragmentLibrary.reload();
                    }
                }
            }
        }
    }

    private void connectReceiver() {
        if (reloadLibraryBroadcastReceiver == null) {
            return;
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RELOAD_LIBRARY_INTENT);
        registerReceiver(reloadLibraryBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        try {
            bottomNavigationDrawerFragment = null;
            unregisterReceiver(reloadLibraryBroadcastReceiver);
            SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        try {
            if (intent.getBooleanExtra("openNowPlaying", false)) {
                openNowPlaying();
            } else {
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                        String type = intent.getType();
                        if (type != null) {
                            Uri uri = intent.getData();
                            EventBus.getDefault().postSticky(new PlaySongAtStart(RealPathUtil.getRealPath(getApplicationContext(), uri)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                fragmentLibrary = null;
                setFragment(SymphonyApplication.getInstance().getPreferenceUtils().getLastOpenedFragment());
            }
        }
    }

    private void resetClipping() {
        if (mainScreenStyle) {
            return;
        }
        if (clipHandler != null) {
            clipHandler.removeCallbacks(clipRunnable);
            clipHandler.postDelayed(clipRunnable, 2500);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (currentFragment == 1 && fragmentFolders != null) {
            fragmentFolders.onRestore();
        }
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > 10 || differenceY > 10);
    }

    private void toolbarMenuClick() {
        if (fragmentLibrary != null) {
            switch (fragmentLibrary.libraryFrameLayout.getCurrentPage()) {
                case 0: {
                    if (libraryToolbar == null || libraryToolbar.getMenu() == null) {
                        return;
                    }
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, libraryToolbar.getMenu());
                    popupMenu.getMenuInflater().inflate(R.menu.menu_songs_fragment, popupMenu.getMenu());
                    switch (SymphonyApplication.getInstance().getPreferenceUtils().getSongSortOrder()) {
                        case MediaStore.Audio.Media.TITLE: {
                            popupMenu.getMenu().findItem(R.id.title).setChecked(true);
                            break;
                        }
                        case MediaStore.Audio.Media.ALBUM: {
                            popupMenu.getMenu().findItem(R.id.album).setChecked(true);
                            break;
                        }
                        case MediaStore.Audio.Media.ARTIST: {
                            popupMenu.getMenu().findItem(R.id.artist).setChecked(true);
                            break;
                        }
                        case MediaStore.Audio.Media.YEAR: {
                            popupMenu.getMenu().findItem(R.id.year).setChecked(true);
                            break;
                        }
                    }
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.title: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.TITLE);
                                EventBus.getDefault().post(new Refresh());
                                break;
                            }
                            case R.id.album: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.ALBUM);
                                EventBus.getDefault().post(new Refresh());
                                break;
                            }
                            case R.id.artist: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.ARTIST);
                                EventBus.getDefault().post(new Refresh());
                                break;
                            }
                            case R.id.year: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setSongSortOrder(MediaStore.Audio.Media.YEAR);
                                EventBus.getDefault().post(new Refresh());
                                break;
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                    break;
                }
                case 1: {
                    if (libraryToolbar == null || libraryToolbar.getMenu() == null) {
                        return;
                    }
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, libraryToolbar.getMenu());
                    popupMenu.getMenuInflater().inflate(R.menu.menu_albums_fragment, popupMenu.getMenu());
                    switch (SymphonyApplication.getInstance().getPreferenceUtils().getAlbumSortOrder()) {
                        case "title": {
                            popupMenu.getMenu().findItem(R.id.title).setChecked(true);
                            break;
                        }
                        case "artist": {
                            popupMenu.getMenu().findItem(R.id.artist).setChecked(true);
                            break;
                        }
                        case "year": {
                            popupMenu.getMenu().findItem(R.id.year).setChecked(true);
                            break;
                        }
                    }
                    switch (SymphonyApplication.getInstance().getPreferenceUtils().getAlbumGrid()) {
                        case 1: {
                            popupMenu.getMenu().findItem(R.id.one).setChecked(true);
                            break;
                        }
                        case 2: {
                            popupMenu.getMenu().findItem(R.id.two).setChecked(true);
                            break;
                        }
                        case 3: {
                            popupMenu.getMenu().findItem(R.id.three).setChecked(true);
                            break;
                        }
                    }
                    switch (SymphonyApplication.getInstance().getPreferenceUtils().getAlbumItemStyle()) {
                        case 0: {
                            popupMenu.getMenu().findItem(R.id.plain).setChecked(true);
                            break;
                        }
                        case 1: {
                            popupMenu.getMenu().findItem(R.id.card).setChecked(true);
                            break;
                        }
                        case 2: {
                            popupMenu.getMenu().findItem(R.id.outline).setChecked(true);
                            break;
                        }
                        case 3: {
                            popupMenu.getMenu().findItem(R.id.colored).setChecked(true);
                            break;
                        }
                        case 4: {
                            popupMenu.getMenu().findItem(R.id.circular).setChecked(true);
                            break;
                        }
                    }
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.title: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumSortOrder("title");
                                EventBus.getDefault().post(new Refresh());
                                break;
                            }
                            case R.id.artist: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumSortOrder("artist");
                                EventBus.getDefault().post(new Refresh());
                                break;
                            }
                            case R.id.year: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumSortOrder("year");
                                EventBus.getDefault().post(new Refresh());
                                break;
                            }
                            case R.id.one: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumGrid(1);
                                EventBus.getDefault().post(new RefreshGrid());
                                break;
                            }
                            case R.id.two: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumGrid(2);
                                EventBus.getDefault().post(new RefreshGrid());
                                break;
                            }
                            case R.id.three: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumGrid(3);
                                EventBus.getDefault().post(new RefreshGrid());
                                break;
                            }
                            case R.id.plain: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumItemStyle(0);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.card: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumItemStyle(1);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.outline: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumItemStyle(2);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.colored: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumItemStyle(3);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.circular: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setAlbumItemStyle(4);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.shuffle_all: {
                                shuffleList(QueryUtils.getAllSongs(getContentResolver(), SymphonyApplication.getInstance().getPreferenceUtils().getSongSortOrder()));
                                break;
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                    break;
                }
                case 2: {
                    if (libraryToolbar == null || libraryToolbar.getMenu() == null) {
                        return;
                    }
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, libraryToolbar.getMenu());
                    popupMenu.getMenuInflater().inflate(R.menu.menu_artists_fragment, popupMenu.getMenu());
                    switch (SymphonyApplication.getInstance().getPreferenceUtils().getArtistGrid()) {
                        case 1: {
                            popupMenu.getMenu().findItem(R.id.one).setChecked(true);
                            break;
                        }
                        case 2: {
                            popupMenu.getMenu().findItem(R.id.two).setChecked(true);
                            break;
                        }
                        case 3: {
                            popupMenu.getMenu().findItem(R.id.three).setChecked(true);
                            break;
                        }
                    }
                    switch (SymphonyApplication.getInstance().getPreferenceUtils().getArtistItemStyle()) {
                        case 0: {
                            popupMenu.getMenu().findItem(R.id.plain).setChecked(true);
                            break;
                        }
                        case 1: {
                            popupMenu.getMenu().findItem(R.id.card).setChecked(true);
                            break;
                        }
                        case 2: {
                            popupMenu.getMenu().findItem(R.id.outline).setChecked(true);
                            break;
                        }
                        case 3: {
                            popupMenu.getMenu().findItem(R.id.colored).setChecked(true);
                            break;
                        }
                        case 4: {
                            popupMenu.getMenu().findItem(R.id.circular).setChecked(true);
                            break;
                        }
                    }
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.one: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistGrid(1);
                                EventBus.getDefault().post(new RefreshGrid());
                                break;
                            }
                            case R.id.two: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistGrid(2);
                                EventBus.getDefault().post(new RefreshGrid());
                                break;
                            }
                            case R.id.three: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistGrid(3);
                                EventBus.getDefault().post(new RefreshGrid());
                                break;
                            }
                            case R.id.plain: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistItemStyle(0);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.card: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistItemStyle(1);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.outline: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistItemStyle(2);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.colored: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistItemStyle(3);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.circular: {
                                SymphonyApplication.getInstance().getPreferenceUtils().setArtistItemStyle(4);
                                EventBus.getDefault().post(new RefreshAdapter());
                                break;
                            }
                            case R.id.shuffle_all: {
                                shuffleList(QueryUtils.getAllSongs(getContentResolver(), SymphonyApplication.getInstance().getPreferenceUtils().getSongSortOrder()));
                                break;
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                    break;
                }
                case 3:
                case 4: {
                    if (libraryToolbar == null || libraryToolbar.getMenu() == null) {
                        return;
                    }
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, libraryToolbar.getMenu());
                    popupMenu.getMenuInflater().inflate(R.menu.menu_genres_playlists_fragment, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.shuffle_all) {
                            shuffleList(QueryUtils.getAllSongs(getContentResolver(), SymphonyApplication.getInstance().getPreferenceUtils().getSongSortOrder()));
                        }
                        return true;
                    });
                    popupMenu.show();
                    break;
                }
            }
        }
    }

    private void openSearchActivity() {
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getApplicationContext(),
                android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(intent, bundle);
    }
}
