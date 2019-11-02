package music.symphony.com.materialmusicv2.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
import music.symphony.com.materialmusicv2.activities.EditMetaDataActivity;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;

import static music.symphony.com.materialmusicv2.utils.artistutils.ArtistUtils.getArtistBioFromNameAndID;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playSingle;
import static music.symphony.com.materialmusicv2.utils.conversionutils.ConversionUtils.covertMilisToTimeString;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;

public class ArtistSongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Song> songs;
    private ArrayList<Album> albums;

    private String artistName;
    private long artistID;
    private String biography;

    private Activity activity;

    private int accentColor;

    private boolean loaded = false;

    public ArtistSongAdapter(ArrayList<Song> songs, ArrayList<Album> albums, int accentColor, String artistName, long artistID, Activity activity) {
        this.songs = new ArrayList<>(songs);
        this.albums = new ArrayList<>(albums);
        this.activity = activity;
        this.accentColor = accentColor;
        this.artistName = artistName;
        this.artistID = artistID;
        this.biography = null;
        fetchBiography();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_song_header_item, parent, false);
            return new ViewHolderSongsHeading(itemView);
        } else if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.biography_item, parent, false);
            return new ViewHolderBiography(itemView);
        } else if (viewType == 2) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.horizontal_recyclerview_album_artist_activity, parent, false);
            return new ViewHolderAlbumRecyclerView(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_song_item, parent, false);
            return new ViewHolderSong(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 2 || position == 4) {
            return 0;
        } else if (position == 1) {
            return 1;
        } else if (position == 3) {
            return 2;
        } else {
            return 3;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0: {
                ViewHolderSongsHeading viewHolderSongsHeading = (ViewHolderSongsHeading) holder;
                if (position == 0) {
                    viewHolderSongsHeading.title.setText(R.string.biography);
                } else if (position == 2) {
                    viewHolderSongsHeading.title.setText(R.string.albums_title);
                } else {
                    viewHolderSongsHeading.title.setText(R.string.songs_title);
                }
                viewHolderSongsHeading.title.setTextColor(accentColor);
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
                    ViewHolderBiography viewHolderBiography = (ViewHolderBiography) holder;
                    viewHolderBiography.expandCollapse.setTextColor(accentColor);
                    viewHolderBiography.biography.setLinkTextColor(accentColor);
                    if (loaded) {
                        if (biography == null || biography.trim().equals("")) {
                            viewHolderBiography.biography.setText(R.string.no_biography);
                            viewHolderBiography.expandCollapse.setVisibility(View.GONE);
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                viewHolderBiography.biography.setText(Html.fromHtml(biography, Html.FROM_HTML_MODE_COMPACT).toString());
                            } else {
                                viewHolderBiography.biography.setText(Html.fromHtml(biography).toString());
                            }
                            viewHolderBiography.biography.setMovementMethod(LinkMovementMethod.getInstance());
                            viewHolderBiography.expandCollapse.setVisibility(View.VISIBLE);
                        }
                    } else {
                        viewHolderBiography.biography.setText(R.string.loading);
                        viewHolderBiography.expandCollapse.setVisibility(View.GONE);
                    }
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
            case 3: {
                try {
                    ViewHolderSong viewHolderSong = (ViewHolderSong) holder;
                    Song currentSong = songs.get(position - 5);
                    viewHolderSong.songName.setText(currentSong.getName());
                    viewHolderSong.songDuration.setText(covertMilisToTimeString(currentSong.getDuration()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public class ViewHolderSong extends RecyclerView.ViewHolder {
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songDuration)
        TextView songDuration;
        @BindView(R.id.menu)
        ImageButton menu;

        @OnLongClick({R.id.artistSongItemBackground})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.artistSongItemBackground, R.id.menu})
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.artistSongItemBackground: {
                    playList(songs, ViewHolderSong.this.getAdapterPosition() - 5);
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_artist_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final int position = ViewHolderSong.this.getAdapterPosition() - 5;
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

        ViewHolderSong(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class ViewHolderSongsHeading extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;

        ViewHolderSongsHeading(View view) {
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

    class ViewHolderBiography extends RecyclerView.ViewHolder {
        @BindView(R.id.biography)
        TextView biography;
        @BindView(R.id.expandCollapse)
        TextView expandCollapse;

        boolean expanded = false;

        @OnClick({R.id.expandCollapse})
        public void onClick(View view) {
            if (view.getId() == R.id.expandCollapse) {
                if (!expanded) {
                    biography.setMaxLines(Integer.MAX_VALUE);
                    expandCollapse.setText(R.string.read_less);
                    expanded = true;
                } else {
                    biography.setMaxLines(4);
                    expandCollapse.setText(R.string.read_more);
                    expanded = false;
                }
            }
        }

        ViewHolderBiography(View view) {
            super(view);
            ButterKnife.bind(this, view);

            biography.setClickable(true);
            biography.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public int getItemCount() {
        return songs.size() + 5;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        if (albums != null) {
            albums.clear();
        }
        if (songs != null) {
            songs.clear();
        }
        activity = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
        notifyItemChanged(0);
        notifyItemChanged(1);
        notifyItemChanged(2);
        notifyItemChanged(4);
    }

    private void fetchBiography() {
        AsyncTask.execute(() -> {
            biography = getArtistBioFromNameAndID(artistName, artistID);
            if (biography != null) {
                biography = biography.trim();
            }
            loaded = true;
            new Handler(Looper.getMainLooper()).post(() -> notifyItemChanged(1));
        });
    }
}
