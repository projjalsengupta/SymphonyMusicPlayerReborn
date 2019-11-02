package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.AlbumActivity;
import music.symphony.com.materialmusicv2.activities.ArtistActivity;
import music.symphony.com.materialmusicv2.activities.EditMetaDataActivity;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.SongDiffCallback;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setTextColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.clearQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playAtSongPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.removeSongFromQueue;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;

public class SongDragNowPlayingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Song> songs;
    private Activity activity;
    private int tintColor = Color.WHITE;
    private int currentSongPosition = 0;

    public SongDragNowPlayingAdapter(ArrayList<Song> songs, Activity activity) {
        this.songs = new ArrayList<>(songs);
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item_now_playing_drag, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (songs == null) {
            return;
        }
        ViewHolder holder = (ViewHolder) viewHolder;

        int pos = position - currentSongPosition;

        final Song currentSong = songs.get(position);

        if (holder.position != null) {
            holder.position.setText(String.valueOf(pos));
            holder.position.setAlpha(pos < 0 ? 0.5f : 1f);
        }
        if (holder.songName != null) {
            holder.songName.setText(currentSong.getName());
            holder.songName.setAlpha(pos < 0 ? 0.5f : 1f);
        }
        if (holder.songArtist != null) {
            holder.songArtist.setText(currentSong.getArtist());
            holder.songArtist.setAlpha(pos < 0 ? 0.5f : 1f);
        }

        setTextColor(tintColor, holder.position, holder.songName, holder.songArtist);
        setColorFilter(tintColor, holder.drageHandle, holder.menu);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
        notifyDataSetChanged();
    }

    public void changeSongs(ArrayList<Song> songs) {
        if (songs == null) {
            return;
        }
        ArrayList<Song> diffUtilOldSongs = new ArrayList<>(this.songs);
        ArrayList<Song> diffUtilNewSongs = new ArrayList<>(songs);
        diffUtilOldSongs.add(0, new Song());
        diffUtilNewSongs.add(0, new Song());
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongDiffCallback(diffUtilNewSongs, diffUtilOldSongs));
        diffResult.dispatchUpdatesTo(this);
        this.songs = new ArrayList<>(songs);
    }

    public void clear() {
        if (songs != null) {
            songs.clear();
            songs = null;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.position)
        TextView position;
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songArtist)
        TextView songArtist;
        @BindView(R.id.menu)
        ImageButton menu;
        @BindView(R.id.dragHandle)
        ImageView drageHandle;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnLongClick({R.id.songItemBackground})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.songItemBackground, R.id.menu})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.songItemBackground: {
                    playAtSongPosition(ViewHolder.this.getAdapterPosition());
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_now_playing_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final int position = ViewHolder.this.getAdapterPosition();
                        final Song song = songs.get(position);
                        switch (item.getItemId()) {
                            case R.id.action_remove_from_queue: {
                                removeSongFromQueue(position);
                                if (songs.size() > 1) {
                                    songs.remove(position);
                                    notifyItemRemoved(position);
                                }
                                break;
                            }
                            case R.id.action_clear_queue: {
                                clearQueue(position);
                                break;
                            }
                            case R.id.action_add_to_playlist: {
                                showAddToPlaylistDialog(activity, song, true);
                                break;
                            }
                            case R.id.action_go_to_album: {
                                Bundle bundle = new Bundle();
                                bundle.putString("ALBUM_NAME", song.getAlbum());
                                bundle.putString("ARTIST_NAME", song.getArtist());
                                bundle.putLong("ALBUM_ID", song.getAlbumId());
                                Intent intent = new Intent(activity, AlbumActivity.class);
                                intent.putExtras(bundle);
                                activity.startActivity(intent);
                                break;
                            }
                            case R.id.action_go_to_artist: {
                                Intent intent = new Intent(activity, ArtistActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("TITLE", song.getArtist());
                                bundle.putLong("ID", song.getArtistId());
                                intent.putExtras(bundle);
                                activity.startActivity(intent);
                                break;
                            }
                            case R.id.action_edit: {
                                Intent intent = new Intent(activity, EditMetaDataActivity.class);
                                intent.putExtra("PATH", songs.get(position).getPath());
                                intent.putExtra("ID", songs.get(position).getId());
                                intent.putExtra("AlBUM_ID", songs.get(position).getAlbumId());
                                activity.startActivity(intent);
                                break;
                            }
                            case R.id.action_share: {
                                shareSong(activity, song.getPath());
                                break;
                            }
                            case R.id.action_set_as_ringtone: {
                                setAudioAsRingtone(new File(song.getPath()), activity);
                                break;
                            }
                            case R.id.action_delete: {
                                if (songs.size() > 1) {
                                    DialogUtils.showYesNoDialog(activity,
                                            R.string.are_you_sure,
                                            R.string.sure_deleting,
                                            new DialogUtils.OnYesNoSelectedListener() {
                                                @Override
                                                public void onYesSelected() {
                                                    if (FileUtils.deleteFile(songs.get(position).getPath(), activity)) {
                                                        songs.remove(position);
                                                        notifyItemRemoved(position);
                                                    }
                                                }

                                                @Override
                                                public void onNoSelected() {

                                                }
                                            });
                                } else {
                                    postToast(R.string.cant_delete_current_song, activity, TOAST_ERROR);
                                }
                                break;
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                    break;
                }
            }
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    public void moveItem(int from, int to) {
        songs.add(to, songs.remove(from));
        notifyItemMoved(from, to);
    }

    public void setCurrentSongPosition(int currentSongPosition) {
        if (songs != null && currentSongPosition >= 0 && currentSongPosition < songs.size()) {
            this.currentSongPosition = currentSongPosition;
            notifyDataSetChanged();
        }
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
}