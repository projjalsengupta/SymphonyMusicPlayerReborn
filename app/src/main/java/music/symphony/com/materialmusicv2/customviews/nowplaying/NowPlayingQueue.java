package music.symphony.com.materialmusicv2.customviews.nowplaying;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.adapters.SongDragNowPlayingAdapter;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.conversionutils.ConversionUtils;
import music.symphony.com.materialmusicv2.utils.misc.DragSortRecycler;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;

public class NowPlayingQueue extends LinearLayout {

    private FastScrollRecyclerView recyclerView;
    private TextView upNext;
    private int tintColor = -1;

    private LinearLayoutManager layoutManager;
    private DragSortRecycler dragSortRecycler;
    private SongDragNowPlayingAdapter songDragNowPlayingAdapter;

    private OnSongMovedListener onSongMovedListener = null;

    public NowPlayingQueue(Context context) {
        super(context);
        init();
    }

    public NowPlayingQueue(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NowPlayingQueue(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.now_playing_queue, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        recyclerView = findViewById(R.id.recyclerView);
        upNext = findViewById(R.id.upNext);
        recyclerView.setItemAnimator(null);
        layoutManager = new LinearLayoutManager(getContext());
        setUpRecyclerView(recyclerView, layoutManager, Color.TRANSPARENT);
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_fall_down);
        recyclerView.setLayoutAnimation(animation);
        recyclerView.setLayoutManager(layoutManager);
        dragSortRecycler = new DragSortRecycler(ThemeUtils.getThemeColorControlHighlight(getContext()));
        dragSortRecycler.setViewHandleId(R.id.dragHandle);
        dragSortRecycler.setOnItemMovedListener((from, to) -> {
            if (from != to) {
                if (songDragNowPlayingAdapter != null) {
                    songDragNowPlayingAdapter.moveItem(from, to);
                }
                if (onSongMovedListener != null) {
                    onSongMovedListener.onSongMoved(from, to);
                }
            }
        });
        recyclerView.addItemDecoration(dragSortRecycler);
        recyclerView.addOnItemTouchListener(dragSortRecycler);
        recyclerView.addOnScrollListener(dragSortRecycler.getScrollListener());
    }

    public void setAccentColor(int tintColor) {
        if (recyclerView != null) {
            recyclerView.setThumbColor(tintColor);
            recyclerView.setThumbInactiveColor(tintColor);
        }
        if (upNext != null) {
            upNext.setTextColor(tintColor);
        }
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
        if (songDragNowPlayingAdapter != null) {
            songDragNowPlayingAdapter.setTintColor(tintColor);
        }
    }

    public void scrollTo(int position) {
        if (recyclerView != null) {
            recyclerView.scrollToPosition(position + 1);
        }
        if (songDragNowPlayingAdapter != null) {
            songDragNowPlayingAdapter.setCurrentSongPosition(position);
            ArrayList<Song> songs = songDragNowPlayingAdapter.getSongs();
            if (songs != null) {
                long totalDuration = 0;
                for (int i = position; i < songs.size(); i++) {
                    totalDuration += songs.get(i).getDuration();
                }
                if (upNext != null) {
                    upNext.setText(String.format(getContext().getString(R.string.up_next), ConversionUtils.covertMilisToTimeString(totalDuration)));
                }
            }
        }
    }

    public void setOnSongMovedListener(OnSongMovedListener onSongMovedListener) {
        this.onSongMovedListener = onSongMovedListener;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        recyclerView = null;
        if (songDragNowPlayingAdapter != null) {
            songDragNowPlayingAdapter.clear();
        }
        songDragNowPlayingAdapter = null;
        layoutManager = null;
        dragSortRecycler = null;

        onSongMovedListener = null;
    }

    public void setSongs(ArrayList<Song> songs, Activity activity) {
        if (songs == null || activity == null) {
            return;
        }
        if (songDragNowPlayingAdapter == null) {
            songDragNowPlayingAdapter = new SongDragNowPlayingAdapter(songs, activity);
            songDragNowPlayingAdapter.setTintColor(tintColor);
            if (recyclerView != null) {
                recyclerView.setAdapter(songDragNowPlayingAdapter);
            }
        } else {
            songDragNowPlayingAdapter.changeSongs(songs);
        }
    }

    public interface OnSongMovedListener {
        void onSongMoved(int from, int to);
    }
}
