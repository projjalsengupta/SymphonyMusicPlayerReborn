package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import music.symphony.com.materialmusicv2.R;

import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;

public class FolderChooserAdapter extends RecyclerView.Adapter<FolderChooserAdapter.FileHolder> {

    private String currentLocation;

    private ArrayList<File> fileList;

    private Callback callback;

    private Activity activity;

    public FolderChooserAdapter(Activity activity, String currentLocation, Callback callback) {
        this.activity = activity;
        this.currentLocation = currentLocation;
        this.callback = callback;
        getFolders();
    }

    @NonNull
    @Override
    public FileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileHolder holder, int position) {
        try {
            if (position == 0 && !currentLocation.equals("/")) {
                holder.name.setTextColor(getThemeTextColorPrimary(activity));
                holder.name.setText("..");
            } else {
                File file = fileList.get(position - (currentLocation.equals("/") ? 0 : 1));
                holder.name.setTextColor(getThemeTextColorPrimary(activity));
                holder.name.setText(file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size() + (currentLocation.equals("/") ? 0 : 1);
    }

    class FileHolder extends RecyclerView.ViewHolder {

        TextView name;

        FileHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);

            (itemView.findViewById(R.id.itemBackground)).setOnClickListener(view -> {
                if (FileHolder.this.getAdapterPosition() == 0 && !currentLocation.equals("/")) {
                    currentLocation = new File(currentLocation).getParent();
                } else {
                    currentLocation = fileList.get(FileHolder.this.getAdapterPosition() - (currentLocation.equals("/") ? 0 : 1)).getPath();
                }
                getFolders();
                notifyDataSetChanged();
                if (callback != null) {
                    callback.locationChanged(currentLocation);
                }
            });
        }
    }

    public interface Callback {
        void locationChanged(String path);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    private void getFolders() {
        fileList = new ArrayList<>();
        try {
            File folder = new File(currentLocation);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.isHidden() && file.isDirectory()) {
                        fileList.add(file);
                    }
                }
            }
            Collections.sort(fileList, (file, t1) -> file.getName().toLowerCase().compareTo(t1.getName().toLowerCase()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCurrentLocation() {
        return currentLocation;
    }
}