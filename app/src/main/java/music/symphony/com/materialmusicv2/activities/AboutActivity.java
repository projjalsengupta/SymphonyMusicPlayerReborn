package music.symphony.com.materialmusicv2.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense3;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.isThemeDarkOrBlack;

public class AboutActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.licenseButton)
    Button licenseButton;
    @BindView(R.id.versionButton)
    Button versionButton;
    @BindView(R.id.rateButton)
    Button rateButton;
    @BindView(R.id.shareButton)
    Button shareButton;
    @BindView(R.id.cardView1)
    CardView cardView1;
    @BindView(R.id.cardView2)
    CardView cardView2;
    @BindView(R.id.cardView3)
    CardView cardView3;

    @OnClick({R.id.licenseButton, R.id.rateButton, R.id.shareButton, R.id.followButton, R.id.privacyPolicy})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.licenseButton: {
                final Notices notices = new Notices();
                notices.addNotice(new Notice("ButterKnife", "https://github.com/JakeWharton/butterknife", "Copyright 2013 Jake Wharton", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("RecyclerView-FastScroll", "https://github.com/timusus/RecyclerView-FastScroll", "Copyright (C) 2016 Tim Malseed", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("MaterialProgressBar", "https://github.com/DreaminginCodeZH/MaterialProgressBar", "Copyright 2015 Zhang Hai", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("The Android Open Source Project", "https://developer.android.com/topic/libraries/support-library/index.html", " Copyright (C) 2010 The Android Open Source Project", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("android-inapp-billing-v3", "https://github.com/anjlab/android-inapp-billing-v3", "Copyright 2014 AnjLab", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("android-slidingactivity", "https://github.com/klinker41/android-slidingactivity", "Copyright (C) 2016 Jacob Klinker", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("CircularSeekBar", "https://github.com/tankery/CircularSeekBar", "Copyright 2013 Matt Joseph", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("MaterialSpinner", "https://github.com/jaredrummler/MaterialSpinner", "Copyright (C) 2016 Jared Rummler", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("BreadcrumbsView", "https://github.com/fython/BreadcrumbsView", "Copyright (c) 2017-2018 Fung Go (fython)", new MITLicense()));
                notices.addNotice(new Notice("ShapeOfView", "https://github.com/florent37/ShapeOfView", "Copyright 2017 Florent37, Inc.", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("EventBus", "https://github.com/greenrobot/EventBus", "Copyright (C) 2012-2017 Markus Junginger, greenrobot", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Glide", "https://github.com/bumptech/glide", "", new License() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String readSummaryTextFromResources(Context context) {
                        return null;
                    }

                    @Override
                    public String readFullTextFromResources(Context context) {
                        return null;
                    }

                    @Override
                    public String getVersion() {
                        return null;
                    }

                    @Override
                    public String getUrl() {
                        return null;
                    }
                }));
                notices.addNotice(new Notice("audio-visualizer-android", "https://github.com/gauravk95/audio-visualizer-android", "Copyright 2018 Gaurav Kumar", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("AppIntro", "https://github.com/AppIntro/AppIntro", "", new ApacheSoftwareLicense20()));
                notices.addNotice(new Notice("Toasty", "https://github.com/GrenderG/Toasty", "Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>", new GnuLesserGeneralPublicLicense3()));

                new LicensesDialog.Builder(AboutActivity.this)
                        .setNotices(notices)
                        .setTitle(R.string.licenses)
                        .setShowFullLicenseText(false)
                        .setIncludeOwnLicense(true)
                        .setThemeResourceId(isThemeDarkOrBlack() ? R.style.DialogThemeDark : R.style.DialogThemeLight)
                        .build()
                        .show();
                break;
            }
            case R.id.rateButton: {
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            }
            case R.id.shareButton: {
                final String appPackageName = getPackageName();
                String shareBody = "Download Symphony Music Player Reborn : https://play.google.com/store/apps/details?id=" + appPackageName;
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)));
                break;
            }
            case R.id.followButton: {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/projjaltweets"));
                startActivity(i);
                break;
            }
            case R.id.privacyPolicy: {
                try {
                    String url = "https://sites.google.com/site/symphonymusicplayerreborn/";
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(getThemePrimaryColor(AboutActivity.this));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(AboutActivity.this, Uri.parse(url));
                } catch (Exception e) {
                    postToast(R.string.error_label, getApplicationContext(), TOAST_ERROR);
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        int colorPrimary = getThemePrimaryColor(this);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(AboutActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        ToolbarUtils.setUpToolbar(toolbar,
                R.string.about,
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                colorPrimary,
                AboutActivity.this,
                this::onBackPressed);

        boolean generalTheme = isThemeDarkOrBlack();
        if (versionButton != null) {
            versionButton.setTextColor(generalTheme ? Color.WHITE : Color.BLACK);
            Drawable img = VectorDrawableCompat.create(getResources(), R.drawable.ic_info_outline_black_24dp, null);
            if (img != null) {
                img.setBounds(0, 0, 60, 60);
                img.setColorFilter(generalTheme ? Color.WHITE : Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
            versionButton.setCompoundDrawables(img, null, null, null);
        }
        if (rateButton != null) {
            rateButton.setTextColor(generalTheme ? Color.WHITE : Color.BLACK);
            Drawable img1 = VectorDrawableCompat.create(getResources(), R.drawable.ic_star_border_black_24dp, null);
            if (img1 != null) {
                img1.setBounds(0, 0, 60, 60);
                img1.setColorFilter(generalTheme ? Color.WHITE : Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
            rateButton.setCompoundDrawables(img1, null, null, null);
        }
        if (shareButton != null) {
            shareButton.setTextColor(generalTheme ? Color.WHITE : Color.BLACK);
            Drawable img2 = VectorDrawableCompat.create(getResources(), R.drawable.ic_share_black_24dp, null);
            if (img2 != null) {
                img2.setBounds(0, 0, 60, 60);
                img2.setColorFilter(generalTheme ? Color.WHITE : Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
            shareButton.setCompoundDrawables(img2, null, null, null);
        }

        if (generalTheme) {
            if (cardView1 != null && cardView2 != null && cardView3 != null) {
                int backgroundColor = ContextCompat.getColor(getApplicationContext(), R.color.md_grey_900);
                cardView1.setCardBackgroundColor(backgroundColor);
                cardView2.setCardBackgroundColor(backgroundColor);
                cardView3.setCardBackgroundColor(backgroundColor);
            }
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
