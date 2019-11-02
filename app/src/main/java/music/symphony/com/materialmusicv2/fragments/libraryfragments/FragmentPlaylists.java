package music.symphony.com.materialmusicv2.fragments.libraryfragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.adapters.PlaylistAdapter;
import music.symphony.com.materialmusicv2.objects.Playlist;
import music.symphony.com.materialmusicv2.objects.events.Refresh;
import music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;

public class FragmentPlaylists extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerView)
    FastScrollRecyclerView recyclerView;

    private ArrayList<Playlist> playlists;
    private PlaylistAdapter playlistAdapter;

    private boolean animated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.simple_recyclerview_layout_for_common_use, container, false);
        ButterKnife.bind(this, rootView);

        setUpRecyclerView(recyclerView, new LinearLayoutManager(getActivity()), Color.TRANSPARENT);
        swipeRefreshLayout.setColorSchemeColors(ThemeUtils.getThemeAccentColor(getActivity()));
        swipeRefreshLayout.setOnRefreshListener(this);

        loadPlaylists();

        return rootView;
    }

    private void loadPlaylists() {
        swipeRefreshLayout.setRefreshing(true);
        AsyncTask.execute(() -> {
            if (getActivity() == null) {
                return;
            }
            playlists = QueryUtils.getAllPlaylists(getActivity().getContentResolver(), MediaStore.Audio.Playlists.NAME, true);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (getActivity() == null) {
                    return;
                }
                if (playlists != null && !playlists.isEmpty() && recyclerView != null) {
                    if (playlistAdapter == null) {
                        playlistAdapter = new PlaylistAdapter(playlists, getActivity());
                        recyclerView.setAdapter(playlistAdapter);
                    } else {
                        playlistAdapter.changePlaylists(playlists);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
                if (!animated) {
                    LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);
                    recyclerView.setLayoutAnimation(animation);
                    animated = true;
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        if (playlists != null) {
            playlists.clear();
            playlists = null;
        }
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        if (playlistAdapter != null) {
            playlistAdapter.clear();
            playlistAdapter = null;
        }
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        loadPlaylists();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Refresh refresh) {
        onRefresh();
    }
}