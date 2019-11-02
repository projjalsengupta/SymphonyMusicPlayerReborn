package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.florent37.shapeofview.shapes.CircleView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;

public class ColorChooserAdapter extends RecyclerView.Adapter<ColorChooserAdapter.ViewHolder> {
    private ArrayList<Integer> colorSchemes;
    private int accentColor;
    private int currentColor;

    private Activity activity;

    private boolean selectorMode = false;

    public ColorChooserAdapter(Activity activity) {
        this.activity = activity;
        colorSchemes = ThemeUtils.getAllAccentColors();
        accentColor = SymphonyApplication.getInstance().getPreferenceUtils().getAccentColor();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.color_chooser_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            holder.colorChooserBackground.setBorderColor(ThemeUtils.getThemeTextColorPrimary(activity));
            if (!selectorMode) {
                int accentColorInt = colorSchemes.get(position * 4 + 3);
                holder.colorView.setBackgroundColor(accentColorInt);
                if (position == (this.accentColor / 4)) {
                    holder.selected.setImageResource(R.drawable.ic_done_black_24dp);
                    holder.selected.setVisibility(View.VISIBLE);
                    setColorFilter(ContrastColor(accentColorInt), holder.selected);
                } else {
                    holder.selected.setVisibility(View.INVISIBLE);
                }
            } else {
                if (position == 0) {
                    holder.colorChooserBackground.setBorderColor(Color.TRANSPARENT);
                    holder.colorView.setBackgroundColor(Color.TRANSPARENT);
                    holder.selected.setImageResource(R.drawable.ic_arrow_back_black_24dp);
                    holder.selected.setVisibility(View.VISIBLE);
                    setColorFilter(ThemeUtils.getThemeTextColorPrimary(activity), holder.selected);
                } else {
                    int accentColorInt = colorSchemes.get(currentColor * 4 + position - 1);
                    holder.colorView.setBackgroundColor(accentColorInt);
                    if ((currentColor * 4 + position - 1) == this.accentColor) {
                        holder.selected.setImageResource(R.drawable.ic_done_black_24dp);
                        holder.selected.setVisibility(View.VISIBLE);
                        setColorFilter(ContrastColor(accentColorInt), holder.selected);
                    } else {
                        holder.selected.setVisibility(View.INVISIBLE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (!selectorMode) {
            return colorSchemes.size() / 4;
        } else {
            return 5;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.colorChooserBackground)
        CircleView colorChooserBackground;
        @BindView(R.id.color)
        View colorView;
        @BindView(R.id.selected)
        ImageView selected;

        @OnClick({R.id.colorChooserBackground})
        public void onClick(View view) {
            if (view.getId() == R.id.colorChooserBackground) {
                if (!selectorMode) {
                    selectorMode = true;
                    currentColor = ViewHolder.this.getAdapterPosition();
                    notifyDataSetChanged();
                } else {
                    if (ViewHolder.this.getAdapterPosition() == 0) {
                        selectorMode = false;
                        notifyDataSetChanged();
                    } else {
                        accentColor = (currentColor * 4) + ViewHolder.this.getAdapterPosition() - 1;
                        notifyDataSetChanged();
                    }
                }
            }
        }

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public int getAccentColor() {
        return accentColor;
    }
}
