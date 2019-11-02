package music.symphony.com.materialmusicv2.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.BuyProActivity;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.VisualizerTypeChanged;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;

import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showSelectorDialog;
import static music.symphony.com.materialmusicv2.utils.fileutils.FileUtils.deleteCache;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference languagePreference = findPreference("language");
        if (languagePreference != null) {
            languagePreference.setOnPreferenceClickListener(preference -> {
                showSelectorDialog(getActivity(),
                        R.string.language,
                        R.array.Languages,
                        SymphonyApplication.getInstance().getPreferenceUtils().getLanguage(),
                        position -> {
                            SymphonyApplication.getInstance().getPreferenceUtils().setLanguage(position);
                            switch (position) {
                                case 0: {
                                    LocaleHelper.setLocale(getActivity(), "en");
                                    break;
                                }
                                case 1: {
                                    LocaleHelper.setLocale(getActivity(), "es");
                                    break;
                                }
                                default: {
                                    LocaleHelper.setLocale(getActivity(), "en");
                                    break;
                                }
                            }
                        });
                return true;
            });
        }

        Preference themePreference = findPreference("theme");
        if (themePreference != null) {
            themePreference.setOnPreferenceClickListener(preference -> {
                DialogUtils.showSelectorDialog(getActivity(),
                        R.string.theme_footer, R.array.Theme,
                        SymphonyApplication.getInstance().getPreferenceUtils().getTheme(),
                        position -> SymphonyApplication.getInstance().getPreferenceUtils().setTheme(position));
                return true;
            });
        }

        Preference imageTypePreference = findPreference("imageType");
        if (imageTypePreference != null) {
            imageTypePreference.setOnPreferenceClickListener(preference -> {
                DialogUtils.showSelectorDialog(getActivity(),
                        R.string.image_type,
                        R.array.ImageType,
                        SymphonyApplication.getInstance().getPreferenceUtils().getImageType(),
                        position -> SymphonyApplication.getInstance().getPreferenceUtils().setImageType(position));
                return true;
            });
        }

        Preference accentColorPreference = findPreference("accentColor");
        if (accentColorPreference != null) {
            accentColorPreference.setOnPreferenceClickListener(preference -> {
                DialogUtils.showColorChooserDialog(getActivity(), R.string.choose_accent_color,
                        position -> SymphonyApplication.getInstance().getPreferenceUtils().setAccentColor(position));
                return true;
            });
        }

        Preference startPagePreference = findPreference("defaultPage");
        if (startPagePreference != null) {
            startPagePreference.setOnPreferenceClickListener(preference -> {
                showSelectorDialog(getActivity(),
                        R.string.start_page,
                        R.array.DefaultPages,
                        SymphonyApplication.getInstance().getPreferenceUtils().getDefaultPage(),
                        position -> SymphonyApplication.getInstance().getPreferenceUtils().setDefaultPage(position));
                return true;
            });
        }

        Preference visualizerTypePreference = findPreference("visualizerType");
        if (visualizerTypePreference != null) {
            visualizerTypePreference.setOnPreferenceClickListener(preference -> {
                if (SymphonyApplication.getInstance().getPreferenceUtils().getDonated()) {
                    showSelectorDialog(getActivity(),
                            R.string.visualizer_type,
                            R.array.VisualizerType,
                            SymphonyApplication.getInstance().getPreferenceUtils().getVisualizerType(),
                            position -> {
                                SymphonyApplication.getInstance().getPreferenceUtils().setVisualizerType(position);
                                EventBus.getDefault().post(new VisualizerTypeChanged(position));
                            });
                } else {
                    openBuyProActivity();
                }
                return true;
            });
        }

        Preference fadePreference = findPreference("fade");
        if (fadePreference != null) {
            fadePreference.setOnPreferenceClickListener(preference -> {
                if (SymphonyApplication.getInstance().getPreferenceUtils().getDonated()) {
                    return true;
                } else {
                    ((SwitchPreference) preference).setChecked(false);
                    openBuyProActivity();
                    return false;
                }
            });
        }

        Preference queueInNotificationPreference = findPreference("showQueueInNotification");
        if (queueInNotificationPreference != null) {
            queueInNotificationPreference.setOnPreferenceClickListener(preference -> {
                if (SymphonyApplication.getInstance().getPreferenceUtils().getDonated()) {
                    return true;
                } else {
                    ((SwitchPreference) preference).setChecked(false);
                    openBuyProActivity();
                    return false;
                }
            });
        }

        Preference showQueueInNotification = findPreference("showQueueInNotification");
        if (showQueueInNotification != null) {
            if (!SymphonyApplication.getInstance().getPreferenceUtils().getUseMediaStyleNotification()) {
                showQueueInNotification.setVisible(true);
            } else {
                showQueueInNotification.setVisible(false);
            }
        }

        Preference clearCachePreference = findPreference("clearCache");
        if (clearCachePreference != null) {
            clearCachePreference.setOnPreferenceClickListener(preference -> {
                try {
                    deleteCache(getActivity());
                    postToast(R.string.cache_cleared, getActivity(), TOAST_SUCCESS);
                } catch (Exception e) {
                    postToast(R.string.error_label, getActivity(), TOAST_ERROR);
                    e.printStackTrace();
                }
                return true;
            });
        }

        Preference nowPlayingStylePreference = findPreference("nowPlayingStyle");
        if (nowPlayingStylePreference != null) {
            nowPlayingStylePreference.setOnPreferenceClickListener(preference -> {
                showSelectorDialog(getActivity(),
                        R.string.now_playing_style,
                        R.array.NowPlayingStyle,
                        SymphonyApplication.getInstance().getPreferenceUtils().getNowPlayingStyle(),
                        position -> {
                            if (SymphonyApplication.getInstance().getPreferenceUtils().getDonated() || position == 1 || position == 2 || position == 4) {
                                SymphonyApplication.getInstance().getPreferenceUtils().setNowPlayingStyle(position);
                            } else {
                                openBuyProActivity();
                            }
                        });
                return true;
            });
        }
        Preference visualizerSpeedPreference = findPreference("visualizerSpeed");
        if (visualizerSpeedPreference != null) {
            visualizerSpeedPreference.setOnPreferenceClickListener(preference -> {
                if (SymphonyApplication.getInstance().getPreferenceUtils().getDonated()) {
                    showSelectorDialog(getActivity(),
                            R.string.visualizer_speed,
                            R.array.VisualizerUpdateInterval,
                            SymphonyApplication.getInstance().getPreferenceUtils().getVisualizerSpeed(),
                            position -> SymphonyApplication.getInstance().getPreferenceUtils().setVisualizerSpeed(position));
                    return true;
                } else {
                    openBuyProActivity();
                    return false;
                }
            });
        }
        Preference tabTitlesModePreference = findPreference("tabTitlesMode");
        if (tabTitlesModePreference != null) {
            tabTitlesModePreference.setOnPreferenceClickListener(preference -> {
                if (SymphonyApplication.getInstance().getPreferenceUtils().getDonated()) {
                    showSelectorDialog(getActivity(),
                            R.string.tab_titles_mode,
                            R.array.TabTitlesMode,
                            SymphonyApplication.getInstance().getPreferenceUtils().getTabTitlesMode(),
                            position -> SymphonyApplication.getInstance().getPreferenceUtils().setTabTitlesMode(position));
                    return true;
                } else {
                    openBuyProActivity();
                    return false;
                }
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //For hiding the divider
        setDivider(new ColorDrawable(Color.TRANSPARENT));
        setDividerHeight(0);

        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if ("useMediaStyleNotification".equals(s)) {
            Preference showQueueInNotification = findPreference("showQueueInNotification");
            if (showQueueInNotification != null) {
                if (!sharedPreferences.getBoolean("useMediaStyleNotification", false)) {
                    showQueueInNotification.setVisible(true);
                } else {
                    showQueueInNotification.setVisible(false);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        SymphonyApplication.getInstance().getPreferenceUtils().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void openBuyProActivity() {
        Intent intent = new Intent(getActivity(), BuyProActivity.class);
        startActivity(intent);
    }
}