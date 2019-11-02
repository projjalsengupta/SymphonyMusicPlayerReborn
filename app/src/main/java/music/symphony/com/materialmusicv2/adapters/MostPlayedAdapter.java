package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
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

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
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
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playSingle;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;

public class MostPlayedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private ArrayList<Song> songs;
    private ArrayList<Integer> playCountList;
    private int pixels;
    private Activity activity;
    private int imageType;

    public MostPlayedAdapter(ArrayList<Song> songs, ArrayList<Integer> playCountList, Activity activity) {
        this.songs = new ArrayList<>(songs);
        this.playCountList = new ArrayList<>(playCountList);
        this.activity = activity;
        if (activity != null) {
            pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
        }
        this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.most_played_song_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        Song currentSong = songs.get(position);

        RequestOptions requestOptions = new RequestOptions();
        if (imageType == 0) {
            requestOptions = requestOptions.transforms(new CenterCrop(), new CircleCrop());
        } else if (imageType == 1) {
            requestOptions = requestOptions.transforms(new CenterCrop());
        } else {
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(24));
        }

        try {
            GlideApp.with(activity)
                    .load(currentSong)
                    .override(pixels, pixels)
                    .apply(requestOptions)
                    .transition(withCrossFade())
                    .error(
                            GlideApp
                                    .with(activity)
                                    .load(R.drawable.ic_blank_album_art)
                                    .override(pixels, pixels)
                                    .apply(requestOptions)
                                    .transition(withCrossFade())
                    )
                    .into(holder.albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.songName.setText(currentSong.getName());
        holder.songArtist.setText(currentSong.getArtist());
        try {
            holder.playCount.setText(String.valueOf(playCountList.get(position)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        ViewHolder holder = (ViewHolder) viewHolder;
        try {
            GlideApp.with(activity).clear(holder.albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onViewRecycled(viewHolder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @NonNull
    @Override
    public String getSectionName(int i) {
        if (i == 0) {
            i = 1;
        }
        if (songs != null && songs.get(i) != null && songs.get(i).getName() != null) {
            return songs.get(i).getName().substring(0, 1);
        }
        return "";
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
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
        @BindView(R.id.playCount)
        TextView playCount;

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

        @OnClick({R.id.songItemBackground, R.id.menu})
        public void onClick(View view) {
            if (activity == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.songItemBackground: {
                    playList(songs, ViewHolder.this.getAdapterPosition());
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_songs, popupMenu.getMenu());
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

    public void changeSongs(ArrayList<Song> songs, ArrayList<Integer> playCountList) {
        if (songs == null || playCountList == null) {
            return;
        }
        ArrayList<Song> diffUtilOldSongs = new ArrayList<>(this.songs);
        ArrayList<Song> diffUtilNewSongs = new ArrayList<>(songs);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongDiffCallback(diffUtilNewSongs, diffUtilOldSongs));
        diffResult.dispatchUpdatesTo(this);
        this.songs = new ArrayList<>(songs);
        for (int i = 0; i < this.playCountList.size(); i++) {
            if (!this.playCountList.get(i).equals(playCountList.get(i))) {
                notifyItemChanged(i);
            }
        }
        this.playCountList = new ArrayList<>(playCountList);
    }
}