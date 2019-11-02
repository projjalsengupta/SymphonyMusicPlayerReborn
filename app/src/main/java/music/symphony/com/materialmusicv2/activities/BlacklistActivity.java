package music.symphony.com.materialmusicv2.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.adapters.RemoveFromBlacklistAdapter;
import music.symphony.com.materialmusicv2.utils.blacklist.BlacklistStore;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class BlacklistActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @OnClick({R.id.fab})
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            DialogUtils.showFileChooserDialog(BlacklistActivity.this,
                    R.string.add_to_blacklist,
                    location -> {
                        BlacklistStore.getInstance(BlacklistActivity.this).addPath(new File(location));
                        postToast(R.string.added_to_blacklist, BlacklistActivity.this, TOAST_SUCCESS);
                        Intent intent = new Intent(RELOAD_LIBRARY_INTENT);
                        sendBroadcast(intent);

                        ArrayList<File> blockedFoldersList = BlacklistStore.getInstance(getApplicationContext()).getFiles();

                        RemoveFromBlacklistAdapter removeFromBlacklistAdapter = new RemoveFromBlacklistAdapter(blockedFoldersList, BlacklistActivity.this);
                        if (recyclerView != null) {
                            recyclerView.setAdapter(removeFromBlacklistAdapter);
                        }
                    });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_blacklist);

        ButterKnife.bind(this);

        int colorPrimary = getThemePrimaryColor(this);
        int colorAccent = getThemeAccentColor(this);
        int contrastColorAccent = ContrastColor(colorAccent);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(BlacklistActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        if (fab != null) {
            fab.setColorFilter(contrastColorAccent, PorterDuff.Mode.SRC_ATOP);
        }

        ToolbarUtils.setUpToolbar(
                toolbar,
                getString(R.string.blacklist),
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                ThemeUtils.getThemePrimaryColor(BlacklistActivity.this),
                BlacklistActivity.this,
                this::onBackPressed,
                false
        );

        ArrayList<File> blockedFoldersList = BlacklistStore.getInstance(getApplicationContext()).getFiles();

        RemoveFromBlacklistAdapter removeFromBlacklistAdapter = new RemoveFromBlacklistAdapter(blockedFoldersList, BlacklistActivity.this);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(removeFromBlacklistAdapter);
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(BlacklistActivity.this, R.anim.layout_animation_fall_down);
            recyclerView.setLayoutAnimation(animation);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent(RELOAD_LIBRARY_INTENT);
        sendBroadcast(intent);
        super.onDestroy();
    }
}
