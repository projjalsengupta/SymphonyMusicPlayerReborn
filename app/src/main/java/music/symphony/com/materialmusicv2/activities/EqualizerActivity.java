package music.symphony.com.materialmusicv2.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.customviews.others.CustomColorSwitchCompat;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.BandLevel;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.BassBoostLevel;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.EqualizerBassboostVirtualizerPresetReverb;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.EquallizerEnabled;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.Preset;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.PresetChanged;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.Reverb;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.UpdateEqualizerActivity;
import music.symphony.com.materialmusicv2.objects.events.controllerevents.VirtualizerLevel;
import music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils;
import music.symphony.com.materialmusicv2.utils.languageutils.LocaleHelper;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;
import music.symphony.com.materialmusicv2.utils.toolbarutils.ToolbarUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemePrimaryColor;

public class EqualizerActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.equalizerBandContainer)
    LinearLayout equalizerBandContainer;
    @BindView(R.id.preset)
    MaterialSpinner preset;
    @BindView(R.id.bassBoost)
    CircularSeekBar bassBoost;
    @BindView(R.id.virtualizer)
    CircularSeekBar virtualizer;
    @BindView(R.id.volume)
    CircularSeekBar volume;
    @BindView(R.id.bassBoostPercentage)
    TextView bassBoostPercentage;
    @BindView(R.id.virtualizerPercentage)
    TextView virtualizerPercentage;
    @BindView(R.id.volumePercentage)
    TextView volumePercentage;
    @BindView(R.id.reverb)
    MaterialSpinner reverb;

    private SeekBar[] seekBars;
    private AudioManager audioManager;

    private SettingsContentObserver settingsContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(ThemeUtils.getTheme(this));

        setContentView(R.layout.activity_equalizer);

        ButterKnife.bind(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        int colorPrimary = getThemePrimaryColor(this);

        if (SymphonyApplication.getInstance().isMiui()) {
            ThemeUtils.setDarkStatusBarIcons(EqualizerActivity.this, ContrastColor(colorPrimary) == Color.BLACK);
        }

        ToolbarUtils.setUpToolbar(toolbar,
                R.string.equalizer,
                new int[]{R.drawable.ic_arrow_back_black_24dp, R.drawable.ic_arrow_back_white_24dp},
                colorPrimary,
                EqualizerActivity.this,
                this::onBackPressed);

        setUpVolume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        getContentResolver().unregisterContentObserver(settingsContentObserver);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    CustomColorSwitchCompat customColorSwitchCompat = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            menu.clear();
        }

        try {
            getMenuInflater().inflate(R.menu.menu_equalizer, menu);
            MenuItem equalizerSwitch = menu != null ? menu.findItem(R.id.action_equalizer_switch) : null;
            if (equalizerSwitch != null) {
                equalizerSwitch.setActionView(R.layout.switch_layout);
                customColorSwitchCompat = (CustomColorSwitchCompat) equalizerSwitch.getActionView();
                int accentColor = getThemeAccentColor(EqualizerActivity.this);
                customColorSwitchCompat.setBgOnColor(accentColor);
                customColorSwitchCompat.setToggleOnColor(accentColor);
                customColorSwitchCompat.setOnCheckedChangeListener((compoundButton, b) -> EventBus.getDefault().post(new EquallizerEnabled(b)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        EventBus.getDefault().post(new UpdateEqualizerActivity());

        return true;
    }

    private void setUpBands(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        try {
            if (equalizerBandContainer != null) {
                try {
                    equalizerBandContainer.removeAllViews();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                seekBars = new SeekBar[ebvpr.numberOfBands];
                short[] bandLevelRange = ebvpr.bandLevelRange;
                for (int i = 0; i < ebvpr.numberOfBands; i++) {
                    int centerFrequency = (ebvpr.centerFrequency[i] / 1000);
                    @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.equalizer_band, null);
                    seekBars[i] = view.findViewById(R.id.seekBar);
                    TextView bandFrequency = view.findViewById(R.id.bandFrequency);
                    bandFrequency.setText(String.format(getString(centerFrequency > 999 ? R.string.freq_text_greater_than_ninenininine : R.string.freq_text_less_than_onezerozerozero), centerFrequency > 999 ? centerFrequency / 1000 : centerFrequency));
                    ColorUtils.setColorFilter(getThemeAccentColor(EqualizerActivity.this), seekBars[i]);
                    seekBars[i].setMax(bandLevelRange[1] - bandLevelRange[0]);
                    seekBars[i].setProgress(ebvpr.bandLevel[i] - bandLevelRange[0]);
                    int finalI = i;
                    seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                            if (b) {
                                EventBus.getDefault().post(new BandLevel((short) finalI, (short) (progress + bandLevelRange[0])));
                                if (preset != null) {
                                    preset.setSelectedIndex(0);
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });
                    equalizerBandContainer.addView(view);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpPreset(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        try {
            if (preset != null) {
                preset.setItems(ebvpr.presets);
                preset.setSelectedIndex(ebvpr.currentPreset);
                preset.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> EventBus.getDefault().post(new Preset(position)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpBassBoostAndVirtualizer(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        int colorAccent = getThemeAccentColor(EqualizerActivity.this);
        try {
            if (bassBoost != null) {
                bassBoost.setCircleStrokeWidth(10f);
                bassBoost.setCircleProgressColor(colorAccent);
                bassBoost.setCircleColor(adjustAlpha(colorAccent, 0.2f));
                bassBoost.setPointerStrokeWidth(15f);
                bassBoost.setPointerColor(colorAccent);
                bassBoost.setPointerHaloColor(colorAccent);
                bassBoost.setMax(1000);
                bassBoost.setProgress(ebvpr.bassBoostLevel);
                bassBoost.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                        if (fromUser) {
                            EventBus.getDefault().post(new BassBoostLevel((int) progress));
                            if (bassBoostPercentage != null) {
                                bassBoostPercentage.setText(String.format("%s%%", String.valueOf((int) progress / 10)));
                            }
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(CircularSeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(CircularSeekBar seekBar) {

                    }
                });
            }
            if (bassBoostPercentage != null) {
                bassBoostPercentage.setText(String.format("%s%%", String.valueOf(ebvpr.bassBoostLevel / 10)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (virtualizer != null) {
                virtualizer.setCircleStrokeWidth(10f);
                virtualizer.setCircleProgressColor(colorAccent);
                virtualizer.setCircleColor(adjustAlpha(colorAccent, 0.2f));
                virtualizer.setPointerStrokeWidth(15f);
                virtualizer.setPointerColor(colorAccent);
                virtualizer.setPointerHaloColor(colorAccent);
                virtualizer.setMax(1000);
                virtualizer.setProgress(ebvpr.virtualizerLevel);
                virtualizer.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                        if (fromUser) {
                            EventBus.getDefault().post(new VirtualizerLevel((int) progress));
                            if (virtualizerPercentage != null) {
                                virtualizerPercentage.setText(String.format("%s%%", String.valueOf((int) progress / 10)));
                            }
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(CircularSeekBar seekBar) {

                    }

                    @Override
                    public void onStartTrackingTouch(CircularSeekBar seekBar) {

                    }
                });
            }
            if (virtualizerPercentage != null) {
                virtualizerPercentage.setText(String.format("%s%%", String.valueOf(ebvpr.virtualizerLevel / 10)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpReverb(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        try {
            if (reverb != null) {
                reverb.setItems(ebvpr.reverbs);
                reverb.setSelectedIndex(ebvpr.currentReverb);
                reverb.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> EventBus.getDefault().post(new Reverb(position)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpVolume() {
        if (volume == null) {
            return;
        }

        try {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            int colorAccent = getThemeAccentColor(EqualizerActivity.this);
            volume.setCircleStrokeWidth(10f);
            volume.setCircleProgressColor(colorAccent);
            volume.setCircleColor(adjustAlpha(colorAccent, 0.2f));
            volume.setPointerStrokeWidth(15f);
            volume.setPointerColor(colorAccent);
            volume.setPointerHaloColor(colorAccent);

            volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            if (volumePercentage != null) {
                volumePercentage.setText(String.format("%s%%", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))));
            }
            volume.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                @Override
                public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                    if (audioManager != null) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) progress, 0);
                    }
                    if (volumePercentage != null && audioManager != null) {
                        volumePercentage.setText(String.format("%s%%", String.valueOf((int) progress * 100 / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))));
                    }
                }

                @Override
                public void onStopTrackingTouch(CircularSeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(CircularSeekBar seekBar) {

                }
            });

            settingsContentObserver = new SettingsContentObserver(new Handler());
            getContentResolver().registerContentObserver(
                    android.provider.Settings.System.CONTENT_URI, true,
                    settingsContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SettingsContentObserver extends ContentObserver {

        SettingsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateVolume();
        }
    }

    private void updateVolume() {
        try {
            if (volume != null) {
                volume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
                volume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            }
            if (volumePercentage != null) {
                volumePercentage.setText(String.format("%s%%", String.valueOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EqualizerBassboostVirtualizerPresetReverb ebvpr) {
        customColorSwitchCompat.setChecked(ebvpr.enabled);
        setUpBands(ebvpr);
        setUpPreset(ebvpr);
        setUpBassBoostAndVirtualizer(ebvpr);
        setUpReverb(ebvpr);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PresetChanged presetChanged) {
        if (seekBars != null) {
            for (int i = 0; i < seekBars.length; i++) {
                seekBars[i].setProgress(presetChanged.bandLevel[i] - presetChanged.bandLevelRange[0]);
            }
        }
    }
}