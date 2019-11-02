package music.symphony.com.materialmusicv2.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.customviews.nowplaying.NowPlayingQueue;
import music.symphony.com.materialmusicv2.objects.events.SongListChanged;
import music.symphony.com.materialmusicv2.objects.events.SongPositionInQueue;
import music.symphony.com.materialmusicv2.utils.controller.Controller;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

public class QueueActivity extends MusicPlayerActivity implements NowPlayingQueue.OnSongMovedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nowPlayingQueue)
    NowPlayingQueue nowPlayingQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_queue);

        ButterKnife.bind(this);

        ToolbarUtils.setUpToolbar(
                toolbar,
                getString(R.string.queue),
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                ThemeUtils.getThemePrimaryColor(QueueActivity.this),
                QueueActivity.this,
                this::onBackPressed,
                false
        );

        nowPlayingQueue.setOnSongMovedListener(QueueActivity.this);
        nowPlayingQueue.setAccentColor(ThemeUtils.getThemeAccentColor(QueueActivity.this));
        nowPlayingQueue.setTintColor(ThemeUtils.getThemeTextColorPrimary(QueueActivity.this));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongListChanged songListChanged) {
        if (nowPlayingQueue != null) {
            nowPlayingQueue.setSongs(songListChanged.songs, QueueActivity.this);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(SongPositionInQueue songPositionInQueue) {
        nowPlayingQueue.scrollTo(songPositionInQueue.position);
    }

    @Override
    public void onSongMoved(int from, int to) {
        Controller.moveQueueItem(from, to);
    }


}
