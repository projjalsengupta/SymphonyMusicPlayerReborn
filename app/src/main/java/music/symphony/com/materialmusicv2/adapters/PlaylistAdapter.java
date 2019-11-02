package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.GenrePlaylistActivity;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.PlaylistDiffCallback;
import music.symphony.com.materialmusicv2.objects.Playlist;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showInputDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.createPlaylist;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.deletePlaylist;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getAllSongs;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getAllSongsFromPlaylist;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getLastPlayedSongs;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getMostPlayedSongs;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getPlaylistID;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;

public class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Playlist> playlists;
    private Activity activity;
    private int textColorPrimary;

    public PlaylistAdapter(ArrayList<Playlist> playlists, Activity activity) {
        this.playlists = new ArrayList<>(playlists);
        this.activity = activity;
        this.textColorPrimary = ThemeUtils.getThemeTextColorPrimary(this.activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item_add, parent, false);
            return new ViewHolderCreatePlaylist(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == 0) {
            ViewHolderCreatePlaylist holder = (ViewHolderCreatePlaylist) viewHolder;
            holder.createPlaylist.setBackgroundColor(getThemeAccentColor(activity));
            holder.createPlaylist.setTextColor(ContrastColor(getThemeAccentColor(activity)));
            holder.createPlaylist.setIconTint(ColorStateList.valueOf(ContrastColor(getThemeAccentColor(activity))));
        } else if (viewHolder.getItemViewType() == 1) {
            ViewHolder holder = (ViewHolder) viewHolder;
            try {
                final Playlist currentPlaylist = playlists.get(position - 1);
                holder.playlistName.setText(currentPlaylist.getName());
                setColorFilter(textColorPrimary, holder.playlistArt);
                switch (currentPlaylist.getName()) {
                    case "Last Added": {
                        holder.playlistArt.setImageResource(R.drawable.ic_queue_black_24dp);
                        break;
                    }
                    case "Recently Played": {
                        holder.playlistArt.setImageResource(R.drawable.ic_history_black_24dp);
                        break;
                    }
                    case "Most Played": {
                        holder.playlistArt.setImageResource(R.drawable.ic_most_played_black_24dp);
                        break;
                    }
                    case "Favorites": {
                        holder.playlistArt.setImageResource(R.drawable.ic_favorite_black_24dp);
                        setColorFilter(ContextCompat.getColor(activity, R.color.md_red_600), holder.playlistArt);
                        break;
                    }
                    default: {
                        holder.playlistArt.setImageResource(R.drawable.ic_queue_music_black_24dp);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return playlists.size() + 1;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.playlistArt)
        ImageView playlistArt;
        @BindView(R.id.playlistItemBackground)
        RelativeLayout playlistItemBackground;
        @BindView(R.id.playlistName)
        TextView playlistName;
        @BindView(R.id.menu)
        ImageButton menu;

        @OnLongClick({R.id.playlistItemBackground})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.playlistItemBackground, R.id.menu})
        public void onClick(View view) {
            final int position = ViewHolder.this.getAdapterPosition() - 1;
            final Playlist playlist = playlists.get(position);
            switch (view.getId()) {
                case R.id.playlistItemBackground: {
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(activity, GenrePlaylistActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("TITLE", playlist.getName());
                        bundle.putString("PATH", null);
                        switch (playlist.getName()) {
                            case "Last Added": {
                                bundle.putLong("ID", -1);
                                bundle.putInt("WHATTODO", 1);
                                break;
                            }
                            case "Recently Played": {
                                bundle.putLong("ID", -1);
                                bundle.putInt("WHATTODO", 2);
                                break;
                            }
                            case "Most Played": {
                                bundle.putLong("ID", -1);
                                bundle.putInt("WHATTODO", 3);
                                break;
                            }
                            default: {
                                bundle.putLong("ID", playlist.getId());
                                bundle.putInt("WHATTODO", 5);
                                break;
                            }
                        }
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                    }, 250);
                    break;
                }
                case R.id.menu: {
                    if (playlist.getName().equals("Last Added") || playlist.getName().equals("Recently Played") || playlist.getName().equals("Most Played")) {
                        PopupMenu popupMenu = new PopupMenu(activity, view);
                        popupMenu.getMenuInflater().inflate(R.menu.menu_playlists_constant, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(item -> {
                            ArrayList<Song> songs = new ArrayList<>();
                            switch (playlist.getName()) {
                                case "Last Added": {
                                    songs = getAllSongs(activity.getContentResolver(), MediaStore.Audio.Media.DATE_ADDED);
                                    Collections.reverse(songs);
                                    break;
                                }
                                case "Recently Played": {
                                    songs = getLastPlayedSongs(activity);
                                    break;
                                }
                                case "Most Played": {
                                    songs = getMostPlayedSongs(activity);
                                    break;
                                }
                            }
                            switch (item.getItemId()) {
                                case R.id.action_play: {
                                    playList(songs, 0);
                                    break;
                                }
                                case R.id.action_add_to_queue: {
                                    addListToQueue(songs);
                                    break;
                                }
                            }
                            return true;
                        });
                        popupMenu.show();
                    } else {
                        PopupMenu popupMenu = new PopupMenu(activity, view);
                        popupMenu.getMenuInflater().inflate(R.menu.menu_playlists, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()) {
                                case R.id.action_play: {
                                    playList(getAllSongsFromPlaylist(activity.getContentResolver(), playlist.getId()), 0);
                                    break;
                                }
                                case R.id.action_add_to_queue: {
                                    addListToQueue(getAllSongsFromPlaylist(activity.getContentResolver(), playlist.getId()));
                                    break;
                                }
                                case R.id.action_delete_playlist: {
                                    deletePlaylist(activity, playlist.getId());
                                    playlists.remove(position);
                                    notifyItemRemoved(position + 1);
                                    break;
                                }
                            }
                            return true;
                        });
                        popupMenu.show();
                    }
                    break;
                }
            }
        }

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class ViewHolderCreatePlaylist extends RecyclerView.ViewHolder {

        @BindView(R.id.create_playlist)
        MaterialButton createPlaylist;

        @OnClick({R.id.create_playlist})
        public void OnClick() {
            showInputDialog(activity,
                    R.string.create_playlist,
                    R.string.enter_playlist_name,
                    input -> {
                        int isPlaylistCreated = createPlaylist(input, activity);
                        if (isPlaylistCreated == 1) {
                            long playlistID = getPlaylistID(input, activity);
                            playlists.add(new Playlist(playlistID, input));
                            notifyItemInserted(playlists.size());
                            postToast(R.string.playlist_created, activity, TOAST_SUCCESS);
                        } else if (isPlaylistCreated == 0) {
                            postToast(R.string.playlist_exists, activity, TOAST_INFO);
                        } else {
                            postToast(R.string.error_label, activity, TOAST_ERROR);
                        }
                    });
        }

        ViewHolderCreatePlaylist(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }

    public void changePlaylists(ArrayList<Playlist> playlists) {
        if (playlists == null) {
            return;
        }
        ArrayList<Playlist> diffUtilOldPlaylists = new ArrayList<>(this.playlists);
        ArrayList<Playlist> diffUtilNewPlaylists = new ArrayList<>(playlists);
        diffUtilOldPlaylists.add(0, new Playlist());
        diffUtilNewPlaylists.add(0, new Playlist());
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PlaylistDiffCallback(diffUtilNewPlaylists, diffUtilOldPlaylists));
        diffResult.dispatchUpdatesTo(this);
        this.playlists = new ArrayList<>(playlists);
    }

    public void clear() {
        if (playlists != null) {
            playlists.clear();
            playlists = null;
        }

        activity = null;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }
}
