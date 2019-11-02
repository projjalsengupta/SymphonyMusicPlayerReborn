package music.symphony.com.materialmusicv2.bottomsheetdialogs;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.tankery.lib.circularseekbar.CircularSeekBar;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.adjustAlpha;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.dipToPixels;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getBottomSheetDialogFragmentTheme;

public class BottomSeekbarInputSheetFragment extends BottomSheetDialogFragment {

    public String tag = "BottomSeekbarInputSheetFragment";

    private Callback callback;

    private Activity activity;

    @BindView(R.id.sheetBackground)
    LinearLayout sheetBackground;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.yesButton)
    MaterialButton yesButton;
    @BindView(R.id.noButton)
    MaterialButton noButton;
    @BindView(R.id.input)
    CircularSeekBar input;

    private int titleRes;
    private boolean titleResSet = false;
    private int messageRes;
    private boolean messageResSet = false;
    private int yesRes;
    private boolean yesResSet = false;
    private int noRes;
    private boolean noResSet = false;

    private int accentColor;
    private boolean accentColorSet = false;
    private int backgroundColor;
    private boolean backgroundColorSet = false;
    private int textColor;
    private boolean textColorSet = false;

    private int max = 0;
    private int min = 0;

    @OnClick({R.id.yesButton, R.id.noButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yesButton: {
                this.dismiss();
                if (callback != null && input != null) {
                    callback.onYesSelected(String.valueOf(((int) input.getProgress()) + min));
                }
                break;
            }
            case R.id.noButton: {
                this.dismiss();
                if (callback != null) {
                    callback.onNoSelected();
                }
                break;
            }
        }
    }

    public BottomSeekbarInputSheetFragment(Activity activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_sheet_seekbar_input_layout, container, false);
        ButterKnife.bind(this, rootView);

        if (activity != null && sheetBackground != null) {
            sheetBackground.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_top_rect_16));
            sheetBackground.getBackground().mutate().setColorFilter(ThemeUtils.getThemeWindowBackgroundColor(activity), android.graphics.PorterDuff.Mode.SRC_IN);
            if (backgroundColorSet) {
                sheetBackground.getBackground().mutate().setColorFilter(backgroundColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        if (title != null && titleResSet) {
            title.setText(titleRes);
        }
        if (title != null && accentColorSet) {
            title.setTextColor(accentColor);
        }
        if (time != null && textColorSet) {
            time.setTextColor(textColor);
        }
        if (message != null && messageResSet) {
            message.setText(messageRes);
        }
        if (message != null && textColorSet) {
            message.setTextColor(textColor);
        }
        if (input != null && min >= 0 && max > min) {
            input.setMax(max - min);
        }
        if (input != null && activity != null && accentColorSet) {
            input.setCircleStrokeWidth(dipToPixels(activity, 6));
            input.setCircleProgressColor(accentColor);
            input.setCircleColor(adjustAlpha(accentColor, 0.2f));
            input.setPointerStrokeWidth(dipToPixels(activity, 12));
            input.setPointerColor(accentColor);
            input.setStartAngle(120f);
            input.setEndAngle(60f);
            input.setPointerHaloColor(accentColor);
        }
        if (input != null) {
            input.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                @Override
                public void onProgressChanged(CircularSeekBar circularSeekBar, float progress, boolean fromUser) {
                    if (time != null) {
                        time.setText(String.format(activity.getString(R.string.minutes_placeholder), (((int) progress) + min)));
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
        if (input != null && time != null && activity != null) {
            time.setText(String.format(activity.getString(R.string.minutes_placeholder), (((int) input.getProgress()) + min)));
        }
        if (yesButton != null && yesResSet) {
            yesButton.setText(yesRes);
        }
        if (yesButton != null && accentColorSet) {
            yesButton.setTextColor(ContrastColor(accentColor));
            yesButton.setBackgroundColor(accentColor);
            if (textColorSet) {
                yesButton.setRippleColor(ColorStateList.valueOf(textColor));
            }
        }
        if (noButton != null && noResSet) {
            noButton.setText(noRes);
        }
        if (noButton != null && textColorSet) {
            noButton.setTextColor(textColor);
            noButton.setBackgroundColor(Color.TRANSPARENT);
            if (accentColorSet) {
                noButton.setRippleColor(ColorStateList.valueOf(accentColor));
            }
        }

        return rootView;
    }

    public void setTitle(int titleRes) {
        this.titleRes = titleRes;
        this.titleResSet = true;
        if (this.title != null) {
            this.title.setText(titleRes);
        }
    }

    public void setMessage(int messageRes) {
        this.messageRes = messageRes;
        this.messageResSet = true;
        if (this.message != null) {
            this.message.setText(messageRes);
        }
    }

    public void setYesText(int yesRes) {
        this.yesRes = yesRes;
        this.yesResSet = true;
        if (this.yesButton != null) {
            this.yesButton.setText(yesRes);
        }
    }

    public void setNoText(int noRes) {
        this.noRes = noRes;
        this.noResSet = true;
        if (this.noButton != null) {
            this.noButton.setText(noRes);
        }
    }

    public void setMinAndMax(int min, int max) {
        if (min >= 0 && max > min) {
            this.min = min;
            this.max = max;
            if (this.input != null) {
                this.input.setMax(max - min);
            }
        }
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
        this.accentColorSet = true;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        this.textColorSet = true;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.backgroundColorSet = true;
    }

    public interface Callback {
        void onYesSelected(String input);

        void onNoSelected();

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public int getTheme() {
        return getBottomSheetDialogFragmentTheme(getActivity());
    }

    public void show() {
        this.show(((AppCompatActivity) activity).getSupportFragmentManager(), tag);
    }
}
