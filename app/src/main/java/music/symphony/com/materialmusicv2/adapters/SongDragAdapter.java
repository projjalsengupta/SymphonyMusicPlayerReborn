package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
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
import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;

import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playSingle;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.movePlaylistItem;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.removeFromPlaylist;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;

public class SongDragAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<Song> songs;
    private int pixels;
    private Activity activity;

    private int imageType;

    private long ID;

    public SongDragAdapter(ArrayList<Song> songs, Activity activity, long ID) {
        this.songs = new ArrayList<>(songs);
        this.activity = activity;
        this.ID = ID;

        if (activity != null) {
            pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
        }

        this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item_drag, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        try {
            holder.albumArt.setImageDrawable(null);
            final Song currentSong = songs.get(position);

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
                        .error(R.drawable.ic_blank_album_art)
                        .apply(requestOptions)
                        .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                            @Override
                            protected void setResource(PaletteBitmap resource) {
                                if (resource != null && resource.bitmap != null) {
                                    holder.albumArt.setImageBitmap(resource.bitmap);
                                    holder.albumArt.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.songName.setText(currentSong.getName());
            holder.songArtist.setText(currentSong.getArtist());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        try {
            GlideApp.with(activity).clear(holder.target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onViewRecycled(viewHolder);
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        try {
            return songs.get(i).getName().substring(0, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }
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
        @BindView(R.id.dragHandle)
        ImageView dragHandle;
        @Nullable
        @BindView(R.id.card)
        MaterialCardView card;

        Target<PaletteBitmap> target;

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
                    playList(songs, ViewHolder.this.getAdapterPosition());
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_playlist_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final int position = ViewHolder.this.getAdapterPosition();
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
                                bundle.putString("TITLE", song.getArtist());
                                bundle.putLong("ID", song.getArtistId());
                                intent.putExtras(bundle);
                                activity.startActivity(intent);
                                break;
                            }
                            case R.id.action_remove_from_playlist: {
                                removeFromPlaylist(activity.getContentResolver(), (int) song.getId(), ID);
                                postToast(R.string.removed_from_playlist, activity, TOAST_SUCCESS);
                                songs.remove(position);
                                notifyItemRemoved(position);
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
                                                    notifyItemRemoved(position);
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

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }

    public boolean moveItem(int from, int to) {
        if (movePlaylistItem(activity.getContentResolver(), ID, from, to)) {
            songs.add(to, songs.remove(from));
            notifyItemMoved(from, to);
            return true;
        }
        return false;
    }
}
