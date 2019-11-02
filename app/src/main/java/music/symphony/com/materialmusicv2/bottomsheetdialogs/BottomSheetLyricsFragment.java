package music.symphony.com.materialmusicv2.bottomsheetdialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.customviews.nowplaying.NowPlayingLyrics;

import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getBottomSheetDialogFragmentTheme;

public class BottomSheetLyricsFragment extends BottomSheetDialogFragment {

    public String tag = "BottomSheetLyricsFragment";

    @BindView(R.id.nowPlayingLyrics)
    NowPlayingLyrics nowPlayingLyrics;
    @BindView(R.id.parent)
    CoordinatorLayout parent;

    private Callback callback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.lyrics, container, false);
        ButterKnife.bind(this, rootView);

        if (callback != null) {
            callback.onReady(nowPlayingLyrics);
        }

        return rootView;
    }

    public interface Callback {
        void onReady(NowPlayingLyrics nowPlayingLyrics);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getTheme() {
        return getBottomSheetDialogFragmentTheme(getActivity());
    }
}
