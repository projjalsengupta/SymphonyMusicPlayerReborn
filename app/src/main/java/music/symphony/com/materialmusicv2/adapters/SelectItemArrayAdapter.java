package music.symphony.com.materialmusicv2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import music.symphony.com.materialmusicv2.R;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;

public class SelectItemArrayAdapter extends ArrayAdapter<String> {

    private int resourceLayout;
    private Context context;
    private int selectedItem = -1;
    private int textColor;

    public SelectItemArrayAdapter(Context context, int resource, String[] items, int textColor) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.context = context;
        this.textColor = textColor;
    }

    public SelectItemArrayAdapter(Context context, int resource, String[] items, int textColor, int selectedItem) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.context = context;
        this.textColor = textColor;
        this.selectedItem = selectedItem;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            v = vi.inflate(resourceLayout, null);
        }

        String text = getItem(position);
        if (text != null) {
            TextView textView = v.findViewById(R.id.text);
            if (textView != null) {
                textView.setText(text);
                textView.setTextColor(textColor);
            }
        }
        ImageView tick = v.findViewById(R.id.tick);
        if (tick != null) {
            setColorFilter(textColor, tick);
            if (position == selectedItem) {
                tick.setVisibility(View.VISIBLE);
            } else {
                tick.setVisibility(View.GONE);
            }
        }
        return v;
    }

}
