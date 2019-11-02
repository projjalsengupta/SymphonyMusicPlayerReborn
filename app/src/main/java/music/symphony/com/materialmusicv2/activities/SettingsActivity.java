package music.symphony.com.materialmusicv2.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.fragments.SettingsFragment;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        int colorPrimary = getThemePrimaryColor(this);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(SettingsActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        ToolbarUtils.setUpToolbar(toolbar,
                R.string.settings,
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                colorPrimary,
                SettingsActivity.this,
                this::onBackPressed);

        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsContainer, new SettingsFragment())
                .commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case "theme":
            case "accentColor":
            case "donated":
            case "language":
                if (sharedPreferences.getString("language", "0").equals("0")) {
                    LocaleHelper.setLocale(SettingsActivity.this, "en");
                } else {
                    LocaleHelper.setLocale(SettingsActivity.this, "es");
                }
                recreate();
                break;
        }
    }
}
