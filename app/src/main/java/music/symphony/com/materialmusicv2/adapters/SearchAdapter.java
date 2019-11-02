package music.symphony.com.materialmusicv2.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;

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
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.AlbumDiffCallback;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.ArtistDiffCallback;
import music.symphony.com.materialmusicv2.adapters.diffcallbacks.SongDiffCallback;
import music.symphony.com.materialmusicv2.glide.PaletteBitmap;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.objects.Artist;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils;
import music.symphony.com.materialmusicv2.utils.fileutils.FileUtils;
import music.symphony.com.materialmusicv2.utils.misc.Statics;

import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playSingle;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getSongsOfAlbum;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getSongsOfArtist;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Song> songs;
    private ArrayList<Album> albums;
    private ArrayList<Artist> artists;
    private int pixels;
    private Activity activity;

    private int imageType;

    private RequestOptions options;

    public SearchAdapter(ArrayList<Song> songs, ArrayList<Album> albums, ArrayList<Artist> artists, Activity activity) {
        this.songs = new ArrayList<>(songs);
        this.albums = new ArrayList<>(albums);
        this.artists = new ArrayList<>(artists);
        this.activity = activity;

        if (activity != null) {
            pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
        }

        this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();

        options = new RequestOptions();
        options = options.override(pixels, pixels);
        options = options.error(R.drawable.ic_blank_album_art);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_header, parent, false);
            return new ViewHolderSearchHeader(itemView);
        } else if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
            return new ViewHolderSong(itemView);
        } else if (viewType == 2) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item_gridsize_1, parent, false);
            return new ViewHolderAlbum(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_item_gridsize_1, parent, false);
            return new ViewHolderArtist(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case 0: {
                ViewHolderSearchHeader holder = (ViewHolderSearchHeader) viewHolder;
                if (position == 0) {
                    if (songs.size() == 0) {
                        holder.itemView.setVisibility(View.GONE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    } else {
                        holder.itemView.setVisibility(View.VISIBLE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    holder.text.setText(R.string.songs_title);
                } else if (position == songs.size() + 1) {
                    if (albums.size() == 0) {
                        holder.itemView.setVisibility(View.GONE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    } else {
                        holder.itemView.setVisibility(View.VISIBLE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    holder.text.setText(R.string.albums_title);
                } else {
                    if (artists.size() == 0) {
                        holder.itemView.setVisibility(View.GONE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    } else {
                        holder.itemView.setVisibility(View.VISIBLE);
                        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    holder.text.setText(R.string.artists_title);
                }
                break;
            }
            case 1: {
                ViewHolderSong holder = (ViewHolderSong) viewHolder;
                Song currentSong = songs.get(position - 1);

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

                break;
            }
            case 2: {
                ViewHolderAlbum holder = (ViewHolderAlbum) viewHolder;
                Album currentAlbum = albums.get(position - songs.size() - 2);

                holder.albumArt.setImageDrawable(null);

                holder.albumName.setText(currentAlbum.getName());
                holder.albumArtist.setText(currentAlbum.getArtist());

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
                            .apply(options)
                            .load(ContentUris.withAppendedId(Statics.artworkUri, currentAlbum.getId()))
                            .apply(requestOptions)
                            .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                                @Override
                                protected void setResource(PaletteBitmap resource) {
                                    try {
                                        if (resource != null && resource.bitmap != null) {
                                            holder.albumArt.setImageBitmap(resource.bitmap);
                                            holder.albumArt.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 3: {
                ViewHolderArtist holder = (ViewHolderArtist) viewHolder;
                Artist currentArtist = artists.get(position - songs.size() - albums.size() - 3);

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
                        .apply(options)
                        .load(currentArtist)
                        .apply(requestOptions)
                        .into(new ImageViewTarget<PaletteBitmap>(holder.artistImage) {
                            @Override
                            protected void setResource(PaletteBitmap resource) {
                                try {
                                    if (resource != null && resource.bitmap != null) {
                                        holder.artistImage.setImageBitmap(resource.bitmap);
                                        holder.artistImage.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_album_art));
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });

                holder.artistName.setText(currentArtist.getName());
                holder.artistSongDetails.setText(String.format(activity.getString(R.string.artist_song_details_placeholder), String.valueOf(currentArtist.getNumberOfAlbums()), String.valueOf(currentArtist.getNumberOfTracks())));
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ViewHolderSong) {
            ViewHolderSong holder = (ViewHolderSong) viewHolder;
            try {
                GlideApp.with(activity).clear(holder.target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (viewHolder instanceof ViewHolderAlbum) {
            ViewHolderAlbum holder = (ViewHolderAlbum) viewHolder;
            try {
                GlideApp.with(activity).clear(holder.target);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (viewHolder instanceof ViewHolderArtist) {
            ViewHolderArtist holder = (ViewHolderArtist) viewHolder;
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
        return 1 + songs.size() + 1 + albums.size() + 1 + artists.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == songs.size() + 1 || position == songs.size() + albums.size() + 2) {
            return 0;
        } else if (position > 0 && position <= songs.size()) {
            return 1;
        } else if (position > songs.size() && position <= songs.size() + albums.size() + 1) {
            return 2;
        } else {
            return 3;
        }
    }

    public void update(ArrayList<Song> songs, ArrayList<Album> albums, ArrayList<Artist> artists) {
        ArrayList<Song> diffUtilOldSongs = new ArrayList<>(this.songs);
        ArrayList<Song> diffUtilNewSongs = new ArrayList<>(songs);
        diffUtilOldSongs.add(0, new Song());
        diffUtilNewSongs.add(0, new Song());
        DiffUtil.DiffResult diffResultSongs = DiffUtil.calculateDiff(new SongDiffCallback(diffUtilNewSongs, diffUtilOldSongs));
        diffResultSongs.dispatchUpdatesTo(this);
        this.songs = new ArrayList<>(songs);

        ArrayList<Album> diffUtilOldAlbums = new ArrayList<>(this.albums);
        ArrayList<Album> diffUtilNewAlbums = new ArrayList<>(albums);
        for (int i = 0; i < songs.size() + 2; i++) {
            diffUtilOldAlbums.add(0, new Album());
            diffUtilNewAlbums.add(0, new Album());
        }
        DiffUtil.DiffResult diffResultAlbums = DiffUtil.calculateDiff(new AlbumDiffCallback(diffUtilNewAlbums, diffUtilOldAlbums));
        diffResultAlbums.dispatchUpdatesTo(this);
        this.albums = new ArrayList<>(albums);

        ArrayList<Artist> diffUtilOldArtists = new ArrayList<>(this.artists);
        ArrayList<Artist> diffUtilNewArtists = new ArrayList<>(artists);
        for (int i = 0; i < songs.size() + albums.size() + 3; i++) {
            diffUtilOldArtists.add(0, new Artist());
            diffUtilNewArtists.add(0, new Artist());
        }
        DiffUtil.DiffResult diffResultArtists = DiffUtil.calculateDiff(new ArtistDiffCallback(diffUtilNewArtists, diffUtilOldArtists));
        diffResultArtists.dispatchUpdatesTo(this);
        this.artists = new ArrayList<>(artists);

        notifyItemChanged(0);
        notifyItemChanged(songs.size() + 1);
        notifyItemChanged(songs.size() + albums.size() + 2);
    }

    class ViewHolderSong extends RecyclerView.ViewHolder {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.songName)
        TextView songName;
        @BindView(R.id.songArtist)
        TextView songArtist;
        @BindView(R.id.menu)
        ImageButton menu;

        Target<PaletteBitmap> target;

        ViewHolderSong(final View view) {
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
                    playList(songs, ViewHolderSong.this.getAdapterPosition() - 1);
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final int position = ViewHolderSong.this.getAdapterPosition() - 1;
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

    class ViewHolderAlbum extends RecyclerView.ViewHolder {
        @BindView(R.id.albumArt)
        ImageView albumArt;
        @BindView(R.id.albumName)
        public TextView albumName;
        @BindView(R.id.albumArtist)
        public TextView albumArtist;
        @BindView(R.id.menu)
        public ImageButton menu;

        Target<PaletteBitmap> target;

        ViewHolderAlbum(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnLongClick({R.id.albumItemOnClicker})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.albumItemOnClicker, R.id.menu})
        void OnClick(View view) {
            final Album album = albums.get(ViewHolderAlbum.this.getAdapterPosition() - songs.size() - 2);
            switch (view.getId()) {
                case R.id.albumItemOnClicker: {
                    Bundle bundle = new Bundle();
                    bundle.putString("ALBUM_NAME", album.getName());
                    bundle.putString("ARTIST_NAME", album.getArtist());
                    bundle.putLong("ALBUM_ID", album.getId());
                    bundle.putInt("ALBUM_YEAR", album.getYear());
                    Intent intent = new Intent(activity, AlbumActivity.class);
                    intent.putExtras(bundle);
                    Bundle options = ActivityOptionsCompat.makeCustomAnimation(activity,
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    activity.startActivity(intent, options);
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_albums, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final ArrayList<Song> songs = getSongsOfAlbum(album.getId(), activity.getContentResolver());
                        switch (item.getItemId()) {
                            case R.id.action_play: {
                                playList(songs, 0);
                                break;
                            }
                            case R.id.action_add_to_queue: {
                                addListToQueue(songs);
                                break;
                            }
                            case R.id.action_add_to_playlist: {
                                showAddToPlaylistDialog(activity, songs);
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

    class ViewHolderArtist extends RecyclerView.ViewHolder {
        @BindView(R.id.artistImage)
        ImageView artistImage;
        @BindView(R.id.artistName)
        TextView artistName;
        @BindView(R.id.artistSongDetails)
        TextView artistSongDetails;
        @BindView(R.id.menu)
        ImageButton menu;

        Target<PaletteBitmap> target;

        @OnLongClick({R.id.artistItemOnClicker})
        public boolean onLongClick() {
            menu.callOnClick();
            return true;
        }

        @OnClick({R.id.artistItemOnClicker, R.id.menu})
        public void onClick(View view) {
            final Artist artist = artists.get(ViewHolderArtist.this.getAdapterPosition() - songs.size() - albums.size() - 3);
            switch (view.getId()) {
                case R.id.artistItemOnClicker: {
                    Intent intent = new Intent(activity, ArtistActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ARTIST_TITLE", artist.getName());
                    bundle.putLong("ARTIST_ID", artist.getId());
                    intent.putExtras(bundle);
                    Bundle options = ActivityOptionsCompat.makeCustomAnimation(activity,
                            android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                    activity.startActivity(intent, options);
                    break;
                }
                case R.id.menu: {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_artists, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (activity == null) {
                            return false;
                        }
                        final ArrayList<Song> songs = getSongsOfArtist(activity.getContentResolver(), MediaStore.Audio.Media.TITLE, artist.getName());
                        switch (item.getItemId()) {
                            case R.id.action_play: {
                                playList(songs, 0);
                                break;
                            }
                            case R.id.action_add_to_queue: {
                                addListToQueue(songs);
                                break;
                            }
                            case R.id.action_add_to_playlist: {
                                showAddToPlaylistDialog(activity, songs);
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

        ViewHolderArtist(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    class ViewHolderSearchHeader extends RecyclerView.ViewHolder {

        @BindView(R.id.text)
        TextView text;

        ViewHolderSearchHeader(final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }
}