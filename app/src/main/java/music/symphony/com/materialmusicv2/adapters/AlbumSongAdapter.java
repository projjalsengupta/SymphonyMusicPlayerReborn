package music.symphony.com.materialmusicv2.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.activities.ArtistActivity;
import music.symphony.com.materialmusicv2.activities.EditMetaDataActivity;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;

import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playSingle;
import static music.symphony.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;

public class AlbumSongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Song> songs;
    private ArrayList<Album> albums;
    private Activity activity;

    private int accentColor;

    public AlbumSongAdapter(ArrayList<Song> songs, ArrayList<Album> albums, int accentColor, Activity activity) {
        this.songs = new ArrayList<>(songs);
        this.albums = new ArrayList<>(albums);
        this.activity = activity;
        this.accentColor = accentColor;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_song_header_item, parent, false);
            return new ViewHolderAlbumSongsHeading(itemView);
        } else if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_song_item, parent, false);
            return new ViewHolderAlbumSong(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_recyclerview_album_artist_activity, parent, false);
            return new ViewHolderAlbumRecyclerView(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == songs.size() + 1) {
            return 0;
        } else if (position > 0 && position <= songs.size()) {
            return 1;
        } else {
            return 2;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0: {
                ViewHolderAlbumSongsHeading viewHolderAlbumSongsHeading = (ViewHolderAlbumSongsHeading) holder;
                if (position == 0) {
                    viewHolderAlbumSongsHeading.title.setText(R.string.songs_title);
                } else {
                    viewHolderAlbumSongsHeading.title.setText(R.string.more_albums_from_this_artist);
                }
                viewHolderAlbumSongsHeading.title.setTextColor(accentColor);
                if (position == songs.size() + 1) {
                    if (albums.size() == 0) {
                        holder.itemView.setVisibility(View.GONE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    } else {
                        holder.itemView.setVisibility(View.VISIBLE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                }
                break;
            }
            case 1: {
                try {
                    ViewHolderAlbumSong viewHolderAlbumSong = (ViewHolderAlbumSong) holder;
                    Song currentSong = songs.get(position - 1);
                    viewHolderAlbumSong.track.setText(String.valueOf(currentSong.getTrackNumber()));
                    viewHolderAlbumSong.songName.setText(currentSong.getName());
                    viewHolderAlbumSong.songDuration.setText(covertMilisToTimeString(currentSong.getDuration()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 2: {
                try {
                    ViewHolderAlbumRecyclerView viewHolderAlbumRecyclerView = (ViewHolderAlbumRecyclerView) holder;
                    if (viewHolderAlbumRecyclerView.recyclerView != null) {
                        viewHolderAlbumRecyclerView.recyclerView.setAdapter(null);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
                        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
                        viewHolderAlbumRecyclerView.recyclerView.setLayoutManager(linearLayoutManager);
                        AlbumSongAlbumAdapter albumSongAlbumAdapter = new AlbumSongAlbumAdapter(albums, activity);
                        viewHolderAlbumRecyclerView.recyclerView.setAdapter(albumSongAlbumAdapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public class ViewHolderAlbumSong extends RecyclerView.ViewHolder {
        @BindView(R.id.track)
        TextView track;
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songDuration)
        TextView songDuration;
        @BindView(R.id.menu)
        ImageButton menu;

        @OnLongClick({R.id.albumSongItemBackground})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.albumSongItemBackground, R.id.menu})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.albumSongItemBackground: {
                    playList(songs, ViewHolderAlbumSong.this.getAdapterPosition() - 1);
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_album_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final int position = ViewHolderAlbumSong.this.getAdapterPosition() - 1;
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
                            case R.id.action_go_to_artist: {
                                Intent intent = new Intent(activity, ArtistActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("ARTIST_TITLE", song.getArtist());
                                bundle.putLong("ARTIST_ID", song.getArtistId());
                                intent.putExtras(bundle);
                                activity.startActivity(intent);
                                break;
                            }
                            case R.id.action_add_to_queue: {
                                addSongToQueue(song);
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

        ViewHolderAlbumSong(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class ViewHolderAlbumSongsHeading extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;

        ViewHolderAlbumSongsHeading(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class ViewHolderAlbumRecyclerView extends RecyclerView.ViewHolder {
        @BindView(R.id.recyclerView)
        RecyclerView recyclerView;

        ViewHolderAlbumRecyclerView(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size() + Math.min(albums.size(), 1) + 2;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (songs != null) {
            songs.clear();
        }
        activity = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
        notifyItemChanged(0);
        notifyItemChanged(songs.size() + 1);
    }
}
