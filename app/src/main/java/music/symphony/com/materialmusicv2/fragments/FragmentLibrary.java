package music.symphony.com.materialmusicv2.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.NowPlayingActivity;
import music.symphony.com.materialmusicv2.activities.QueueActivity;
import music.symphony.com.materialmusicv2.customviews.library.LibraryBottomPlaybackController;
import music.symphony.com.materialmusicv2.customviews.library.LibraryFrameLayout;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.pauseOrResumePlayer;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playNext;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playPrevious;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class FragmentLibrary extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.bottomNavigation)
    BottomNavigationView bottomNavigation;
    @BindView(R.id.libraryFrameLayout)
    public LibraryFrameLayout libraryFrameLayout;
    @BindView(R.id.libraryBottomPlaybackController)
    LibraryBottomPlaybackController libraryBottomPlaybackController;

    private int tabTitlesMode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_library, container, false);

        ButterKnife.bind(this, rootView);

        tabTitlesMode = SymphonyApplication.getInstance().getPreferenceUtils().getTabTitlesMode();

        setUpLibraryViewPager();

        setUpBottomController();

        return rootView;
    }

    private void setUpLibraryViewPager() {
        int defaultPage = SymphonyApplication.getInstance().getPreferenceUtils().getDefaultPage();

        if (bottomNavigation != null) {
            int colorPrimary = getThemePrimaryColor(getActivity());
            int colorAccent = getThemeAccentColor(getActivity());

            switch (defaultPage) {
                case 0: {
                    bottomNavigation.setSelectedItemId(R.id.action_songs);
                    break;
                }
                case 1: {
                    bottomNavigation.setSelectedItemId(R.id.action_albums);
                    break;
                }
                case 2: {
                    bottomNavigation.setSelectedItemId(R.id.action_artists);
                    break;
                }
                case 3: {
                    bottomNavigation.setSelectedItemId(R.id.action_genres);
                    break;
                }
                case 4: {
                    bottomNavigation.setSelectedItemId(R.id.action_playlists);
                    break;
                }
            }

            switch (tabTitlesMode) {
                case 0: {
                    bottomNavigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
                    break;
                }
                case 1: {
                    bottomNavigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);
                    break;
                }
                case 2: {
                    bottomNavigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);
                    break;
                }
            }

            bottomNavigation.setBackgroundColor(colorPrimary);

            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_checked},
                    new int[]{-android.R.attr.state_checked},
            };

            int[] colors = new int[]{
                    colorAccent,
                    adjustAlpha(ContrastColor(colorPrimary), 0.5f),
            };

            ColorStateList colorStateList = new ColorStateList(states, colors);
            bottomNavigation.setItemIconTintList(colorStateList);
            bottomNavigation.setItemTextColor(colorStateList);

            bottomNavigation.setOnNavigationItemSelectedListener(FragmentLibrary.this);
        }

        if (libraryFrameLayout == null) {
            return;
        }

        setFragment(defaultPage);
    }

    private void setFragment(int position) {
        try {
            if (libraryFrameLayout != null) {
                libraryFrameLayout.setFragment(((AppCompatActivity) getActivity()), position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        if (libraryFrameLayout != null) {
            libraryFrameLayout.reload();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (libraryFrameLayout == null) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.action_songs: {
                setFragment(0);
                break;
            }
            case R.id.action_albums: {
                setFragment(1);
                break;
            }
            case R.id.action_artists: {
                setFragment(2);
                break;
            }
            case R.id.action_genres: {
                setFragment(3);
                break;
            }
            case R.id.action_playlists: {
                setFragment(4);
                break;
            }
        }
        return true;
    }

    private void setUpBottomController() {
        if (libraryBottomPlaybackController == null) {
            return;
        }
        boolean mainScreenStyle = SymphonyApplication.getInstance().getPreferenceUtils().getMainScreenStyle();
        if (!mainScreenStyle) {
            libraryBottomPlaybackController.setVisibility(View.GONE);
            return;
        }
        libraryBottomPlaybackController.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(getActivity()));
        libraryBottomPlaybackController.setProgressColor(ContrastColor(ThemeUtils.getThemeWindowBackgroundColor(getActivity())), getThemeAccentColor(getActivity()));
        libraryBottomPlaybackController.setOnClickEventDetectedListener(new LibraryBottomPlaybackController.OnClickEventDetectedListener() {
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
                openNowPlaying();
            }

            @Override
            public void onOpenPlayingQueueClicked() {
                openPlayingQueue();
            }
        });
    }

    private void openNowPlaying() {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongs() != null && SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(getActivity(), NowPlayingActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getActivity(), TOAST_INFO);
        }
    }

    private void openPlayingQueue() {
        if (SymphonyApplication.getInstance().getPlayingQueueManager().getSongs() != null && SymphonyApplication.getInstance().getPlayingQueueManager().getSongs().size() > 0) {
            Intent intent = new Intent(getActivity(), QueueActivity.class);
            startActivity(intent);
        } else {
            postToast(R.string.no_song_is_playing, getActivity(), TOAST_INFO);
        }
    }
}