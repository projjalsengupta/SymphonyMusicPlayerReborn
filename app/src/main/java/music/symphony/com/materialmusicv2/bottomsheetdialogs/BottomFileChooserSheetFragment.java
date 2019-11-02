package music.symphony.com.materialmusicv2.bottomsheetdialogs;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.adapters.FolderChooserAdapter;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.recyclerviewutils.RecyclerViewUtils.setUpRecyclerView;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getBottomSheetDialogFragmentTheme;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;

public class BottomFileChooserSheetFragment extends BottomSheetDialogFragment {

    public String tag = "BottomFileChooserSheetFragment";

    private Callback callback;

    private Activity activity;

    @BindView(R.id.sheetBackground)
    LinearLayout sheetBackground;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.yesButton)
    MaterialButton yesButton;

    private int yesRes;
    private boolean yesResSet = false;

    private int accentColor;
    private boolean accentColorSet = false;
    private int backgroundColor;
    private boolean backgroundColorSet = false;

    private FolderChooserAdapter folderChooserAdapter;

    public BottomFileChooserSheetFragment(Activity activity) {
        this.activity = activity;
    }

    @OnClick({R.id.yesButton})
    public void onClick(View view) {
        if (view.getId() == R.id.yesButton) {
            this.dismiss();
            if (callback != null && folderChooserAdapter != null) {
                callback.onYesSelected(folderChooserAdapter.getCurrentLocation());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_sheet_file_chooser_layout, container, false);
        ButterKnife.bind(this, rootView);

        if (activity != null && sheetBackground != null) {
            sheetBackground.setBackground(ContextCompat.getDrawable(activity, R.drawable.rounded_top_rect_16));
            sheetBackground.getBackground().mutate().setColorFilter(ThemeUtils.getThemeWindowBackgroundColor(activity), android.graphics.PorterDuff.Mode.SRC_IN);
            if (backgroundColorSet) {
                sheetBackground.getBackground().mutate().setColorFilter(backgroundColor, android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }

        if (title != null) {
            title.setText(Environment.getExternalStorageDirectory().getPath());
        }
        if (title != null && accentColorSet) {
            title.setTextColor(accentColor);
        }
        if (yesButton != null && yesResSet) {
            yesButton.setText(yesRes);
        }
        if (yesButton != null && accentColorSet) {
            yesButton.setTextColor(ContrastColor(accentColor));
            yesButton.setBackgroundColor(accentColor);
            yesButton.setRippleColor(ColorStateList.valueOf(ContrastColor(accentColor)));
        }

        setUpRecyclerView(recyclerView, new LinearLayoutManager(activity), getThemeAccentColor(activity));
        folderChooserAdapter = new FolderChooserAdapter(activity, Environment.getExternalStorageDirectory().getPath(), path -> {
            if (title != null) {
                title.setText(path);
            }
        });
        recyclerView.setAdapter(folderChooserAdapter);

        return rootView;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
        this.accentColorSet = true;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.backgroundColorSet = true;
    }

    public void setYesText(int yesRes) {
        this.yesRes = yesRes;
        this.yesResSet = true;
        if (this.yesButton != null) {
            this.yesButton.setText(yesRes);
        }
    }

    @Override
    public int getTheme() {
        return getBottomSheetDialogFragmentTheme(getActivity());
    }

    public void show() {
        this.show(((AppCompatActivity) activity).getSupportFragmentManager(), tag);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onYesSelected(String location);
    }

}
