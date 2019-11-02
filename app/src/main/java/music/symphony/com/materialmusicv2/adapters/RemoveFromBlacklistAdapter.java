package music.symphony.com.materialmusicv2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.utils.blacklist.BlacklistStore;

public class RemoveFromBlacklistAdapter extends RecyclerView.Adapter<RemoveFromBlacklistAdapter.ViewHolder> {
    private ArrayList<File> folders;
    private Context context;

    public RemoveFromBlacklistAdapter(ArrayList<File> folders, Context context) {
        this.folders = new ArrayList<>(folders);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.remove_from_blacklist_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        try {
            final File currentFolder = folders.get(position);
            holder.folderName.setText(currentFolder.getName());
            holder.folderPath.setText(currentFolder.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.folderName)
        TextView folderName;
        @BindView(R.id.folderPath)
        TextView folderPath;

        @OnClick({R.id.removeFromBlacklist})
        public void onClick() {
            BlacklistStore.getInstance(context).removePath(folders.get(ViewHolder.this.getAdapterPosition()));
            folders.remove(ViewHolder.this.getAdapterPosition());
            notifyItemRemoved(ViewHolder.this.getAdapterPosition());
        }

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        context = null;
    }
}
