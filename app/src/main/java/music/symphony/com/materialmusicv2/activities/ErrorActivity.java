package music.symphony.com.materialmusicv2.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class ErrorActivity extends AppCompatActivity {

    private static final String SCHEME = "package";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appInfoButton)
    Button appInfoButton;

    @OnClick({R.id.fab, R.id.appInfoButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab: {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "error.symphony@gmail.com", null));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Error report. Symphony Music Player Reborn");
                intent.putExtra(Intent.EXTRA_TEXT, "Error happened in this line" + "\n" + error);
                startActivity(Intent.createChooser(intent, "Send Email..."));
                break;
            }
            case R.id.appInfoButton: {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts(SCHEME, getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                break;
            }
        }
    }

    private String error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_error);

        ButterKnife.bind(this);

        int colorPrimary = getThemePrimaryColor(this);
        int contrastColor = ContrastColor(colorPrimary);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(ErrorActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        ToolbarUtils.setUpToolbar(toolbar,
                R.string.error_label,
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                colorPrimary,
                ErrorActivity.this,
                this::onBackPressed);

        error = getIntent().getStringExtra("error");

        if (appInfoButton != null) {
            appInfoButton.setTextColor(contrastColor);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
