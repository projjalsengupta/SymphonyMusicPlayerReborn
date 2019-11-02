package music.symphony.com.materialmusicv2.bottomsheetdialogs;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.adapters.SelectItemArrayAdapter;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getBottomSheetDialogFragmentTheme;

public class BottomSelectorSheetFragment extends BottomSheetDialogFragment {
    public String tag = "BottomSelectorSheetFragment";

    private Callback callback;

    private Activity activity;

    @BindView(R.id.sheetBackground)
    LinearLayout sheetBackground;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.noButton)
    MaterialButton noButton;

    private int titleRes;
    private boolean titleResSet = false;
    private int noRes;
    private boolean noResSet = false;

    private int accentColor;
    private boolean accentColorSet = false;
    private int backgroundColor;
    private boolean backgroundColorSet = false;
    private int textColor;
    private boolean textColorSet = false;

    private ArrayList<String> list;
    private boolean listSet = false;
    private int listRes;
    private boolean listResSet = false;

    private int selectedItem = -1;

    @OnClick({R.id.noButton})
    public void onClick(View view) {
        if (view.getId() == R.id.noButton) {
            this.dismiss();
            if (callback != null) {
                callback.onNoSelected();
            }
        }
    }

    public BottomSelectorSheetFragment(Activity activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_sheet_selector_layout, container, false);
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

        if (activity != null && listView != null && listSet && list != null && list.size() > 0) {
            if (selectedItem == -1) {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, list.toArray(new String[0]), textColor));
            } else {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, list.toArray(new String[0]), textColor, selectedItem));
            }
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                this.dismiss();
                if (callback != null) {
                    callback.listItemSelected(i);
                }
            });
        }

        if (activity != null && listView != null && listResSet) {
            if (selectedItem == -1) {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, getResources().getStringArray(listRes), textColor));
            } else {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, getResources().getStringArray(listRes), textColor, selectedItem));
            }
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                this.dismiss();
                if (callback != null) {
                    callback.listItemSelected(i);
                }
            });
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

    public void setList(@NonNull ArrayList<String> list) {
        this.list = new ArrayList<>(list);
        this.listSet = true;
        if (activity != null && listView != null && this.list.size() > 0) {
            if (selectedItem == -1) {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, list.toArray(new String[0]), textColor));
            } else {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, list.toArray(new String[0]), textColor, selectedItem));
            }
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                this.dismiss();
                if (callback != null) {
                    callback.listItemSelected(i);
                }
            });
        }
    }

    public void setList(int listRes) {
        this.listRes = listRes;
        this.listResSet = true;
        if (activity != null && listView != null && this.list.size() > 0) {
            if (selectedItem == -1) {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, getResources().getStringArray(listRes), textColor));
            } else {
                listView.setAdapter(new SelectItemArrayAdapter(activity, R.layout.select_item_list_item, getResources().getStringArray(listRes), textColor, selectedItem));
            }
            listView.setOnItemClickListener((adapterView, view, i, l) -> {
                this.dismiss();
                if (callback != null) {
                    callback.listItemSelected(i);
                }
            });
        }
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public interface Callback {
        void listItemSelected(int position);

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
