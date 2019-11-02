package music.symphony.com.materialmusicv2.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.AlbumActivity;
import music.symphony.com.materialmusicv2.activities.ArtistActivity;
import music.symphony.com.materialmusicv2.activities.EditMetaDataActivity;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.SongDiffCallback;
import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;

import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.ContrastColor;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playSingle;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.shuffleList;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeAccentColor;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<Song> songs;
    private int pixels;
    private Activity activity;

    private int imageType;

    private boolean showShuffleAll;

    public SongAdapter(ArrayList<Song> songs, Activity activity, boolean showShuffleAll) {
        this.songs = new ArrayList<>(songs);
        this.activity = activity;
        this.showShuffleAll = showShuffleAll;

        if (activity != null) {
            pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
        }

        this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item_shuffle_all, parent, false);
            return new ViewHolderSongHeader(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
            return new ViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder.getItemViewType() == 0) {
            ViewHolderSongHeader holder = (ViewHolderSongHeader) viewHolder;
            holder.shuffleAll.setTextColor(ColorStateList.valueOf(ContrastColor(getThemeAccentColor(activity))));
            holder.shuffleAll.setIconTint(ColorStateList.valueOf(ContrastColor(getThemeAccentColor(activity))));
            holder.shuffleAll.setBackgroundColor(getThemeAccentColor(activity));
        } else if (viewHolder.getItemViewType() == 1) {
            ViewHolder holder = (ViewHolder) viewHolder;
            Song currentSong = songs.get(position - (showShuffleAll ? 1 : 0));
            try {
                RequestOptions requestOptions = new RequestOptions();
                if (imageType == 0) {
                    requestOptions = requestOptions.transforms(new CenterCrop(), new CircleCrop());
                } else if (imageType == 1) {
                    requestOptions = requestOptions.transforms(new CenterCrop());
                } else {
                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(24));
                }
                holder.target = GlideApp.with(activity)
                        .as(PaletteBitmap.class)
                        .load(currentSong)
                        .override(pixels, pixels)
                        .apply(requestOptions)
                        .error(R.drawable.ic_blank_album_art)
                        .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                            @Override
                            protected void setResource(PaletteBitmap resource) {
                                try {
                                    if (resource != null && resource.bitmap != null) {
                                        holder.albumArt.setImageBitmap(resource.bitmap);
                                        holder.albumArt.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.songName.setText(currentSong.getName());
            holder.songArtist.setText(currentSong.getArtist());
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.getItemViewType() == 1) {
            ViewHolder holder = (ViewHolder) viewHolder;
            try {
                GlideApp.with(activity).clear(holder.target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onViewRecycled(viewHolder);
    }

    @Override
    public int getItemCount() {
        return songs.size() + (showShuffleAll ? 1 : 0);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        if (showShuffleAll && i == 0) {
            i = 1;
        }
        if (songs != null && songs.get(i - (showShuffleAll ? 1 : 0)) != null && songs.get(i - (showShuffleAll ? 1 : 0)).getName() != null) {
            return songs.get(i - (showShuffleAll ? 1 : 0)).getName().substring(0, 1);
        }
        return "";

    }

    @Override
    public int getItemViewType(int position) {
        return showShuffleAll ? (position == 0 ? 0 : 1) : 1;
    }

    public void changeSongs(ArrayList<Song> songs) {
        if (songs == null) {
            return;
        }
        ArrayList<Song> diffUtilOldSongs = new ArrayList<>(this.songs);
        ArrayList<Song> diffUtilNewSongs = new ArrayList<>(songs);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongDiffCallback(diffUtilNewSongs, diffUtilOldSongs));
        diffResult.dispatchUpdatesTo(this);
        this.songs = new ArrayList<>(songs);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songArtist)
        TextView songArtist;
        @BindView(R.id.menu)
        ImageButton menu;

        Target<PaletteBitmap> target;

        ViewHolder(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnLongClick({R.id.songItemBackground})
        public boolean onLongClick() {
            if (menu != null) {
                menu.callOnClick();
            }
            return true;
        }

        @SuppressLint("RestrictedApi")
        @OnClick({R.id.songItemBackground, R.id.menu})
        public void onClick(View view) {
            if (activity == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.songItemBackground: {
                    playList(songs, ViewHolder.this.getAdapterPosition() - (showShuffleAll ? 1 : 0));
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final int position = ViewHolder.this.getAdapterPosition() - (showShuffleAll ? 1 : 0);
                        final Song song = songs.get(position);
                        switch (item.getItemId()) {
                            case R.id.action_play: {
                                playSingle(song);
                                break;
                            }
                            case R.id.action_play_next: {
                                addSongToNextPosition(song);
                                break;
                            }
                            case R.id.action_add_to_playlist: {
                                showAddToPlaylistDialog(activity, song, true);
                                break;
                            }
                            case R.id.action_add_to_queue: {
                                addSongToQueue(song);
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
                                bundle.putString("ARTIST_TITLE", song.getArtist());
                                bundle.putLong("ARTIST_ID", song.getArtistId());
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
                                DialogUtils.showYesNoDialog(activity,
                                        R.string.are_you_sure,
                                        R.string.sure_deleting,
                                        new DialogUtils.OnYesNoSelectedListener() {
                                            @Override
                                            public void onYesSelected() {
                                                if (FileUtils.deleteFile(songs.get(position).getPath(), activity)) {
                                                    songs.remove(position);
                                                    notifyItemRemoved(position + 1);
                                                }
                                            }

                                            @Override
                                            public void onNoSelected() {
                                            }
                                        });
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

    class ViewHolderSongHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.shuffleAll)
        MaterialButton shuffleAll;

        ViewHolderSongHeader(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick({R.id.shuffleAll})
        public void OnClick(View view) {
            if (view.getId() == R.id.shuffleAll) {
                shuffleList(songs);
            }
        }
    }

    public void clear() {
        if (songs != null) {
            songs.clear();
            songs = null;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }
}