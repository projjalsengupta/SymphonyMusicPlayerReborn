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
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getBottomSheetDialogFragmentTheme;

public class BottomYesNoSheetFragment extends BottomSheetDialogFragment {

    public String tag = "BottomYesNoSheetFragment";

    private Callback callback;

    private Activity activity;

    @BindView(R.id.sheetBackground)
    LinearLayout sheetBackground;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.yesButton)
    MaterialButton yesButton;
    @BindView(R.id.noButton)
    MaterialButton noButton;

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

    @OnClick({R.id.yesButton, R.id.noButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yesButton: {
                this.dismiss();
                if (callback != null) {
                    callback.onYesSelected();
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

    public BottomYesNoSheetFragment(Activity activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_sheet_yes_no_layout, container, false);
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
        if (message != null && messageResSet) {
            message.setText(messageRes);
        }
        if (message != null && textColorSet) {
            message.setTextColor(textColor);
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
        void onYesSelected();

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
