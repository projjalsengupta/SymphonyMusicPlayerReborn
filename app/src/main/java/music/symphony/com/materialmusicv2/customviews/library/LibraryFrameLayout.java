package music.symphony.com.materialmusicv2.customviews.library;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import music.symphony.com.materialmusicv2.fragments.libraryfragments.FragmentAlbums;
import music.symphony.com.materialmusicv2.fragments.libraryfragments.FragmentArtists;
import music.symphony.com.materialmusicv2.fragments.libraryfragments.FragmentGenres;
import music.symphony.com.materialmusicv2.fragments.libraryfragments.FragmentPlaylists;
import music.symphony.com.materialmusicv2.fragments.libraryfragments.FragmentSongs;

public class LibraryFrameLayout extends FrameLayout {

    public LibraryFrameLayout(Context context) {
        super(context);
    }

    public LibraryFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    Fragment currentFragment = null;

    private int currentPage = -1;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setFragment(AppCompatActivity activity, int index) {
        if (activity == null) {
            return;
        }
        if (currentPage == index) {
            return;
        }
        currentPage = index;
        final FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        currentFragment = new Fragment();
        switch (index) {
            case 0: {
                currentFragment = new FragmentSongs();
                break;
            }
            case 1: {
                currentFragment = new FragmentAlbums();
                break;
            }
            case 2: {
                currentFragment = new FragmentArtists();
                break;
            }
            case 3: {
                currentFragment = new FragmentGenres();
                break;
            }
            case 4: {
                currentFragment = new FragmentPlaylists();
                break;
            }
        }
        transaction.replace(getId(), currentFragment);
        transaction.commit();
    }

    public void reload() {
        if (currentFragment == null) {
            return;
        }
        switch (currentPage) {
            case 0: {
                ((FragmentSongs) currentFragment).onRefresh();
                break;
            }
            case 1: {
                ((FragmentAlbums) currentFragment).onRefresh();
                break;
            }
            case 2: {
                ((FragmentArtists) currentFragment).onRefresh();
                break;
            }
            case 3: {
                ((FragmentGenres) currentFragment).onRefresh();
                break;
            }
            case 4: {
                ((FragmentPlaylists) currentFragment).onRefresh();
                break;
            }
        }
    }
}
