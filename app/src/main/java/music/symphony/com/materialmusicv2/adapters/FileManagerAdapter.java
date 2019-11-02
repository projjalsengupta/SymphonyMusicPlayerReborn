package music.symphony.com.materialmusicv2.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import music.symphony.com.materialmusicv2.GlideApp;
import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.activities.AlbumActivity;
import music.symphony.com.materialmusicv2.activities.ArtistActivity;
import music.symphony.com.materialmusicv2.activities.EditMetaDataActivity;
import music.symphony.com.materialmusicv2.objects.FileList;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.blacklist.BlacklistStore;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static music.symphony.com.materialmusicv2.utils.colorutils.ColorUtils.setColorFilter;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addListToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToNextPosition;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.addSongToQueue;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playList;
import static music.symphony.com.materialmusicv2.utils.controller.Controller.playSingle;
import static music.symphony.com.materialmusicv2.utils.drawableutils.DialogUtils.showAddToPlaylistDialog;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.setAudioAsRingtone;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getAllSongsOfFolder;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getSongFromPath;
import static music.symphony.com.materialmusicv2.utils.shareutils.ShareUtils.shareSong;
import static music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils.getThemeTextColorPrimary;

public class FileManagerAdapter extends RecyclerView.Adapter<FileManagerAdapter.FileHolder> {

    private FileList fileList;
    private Callback callback;

    private int defaultTextColor;
    private Activity activity;
    private int pixels;

    private String currentLocation;

    private int imageType;

    public FileManagerAdapter(Activity activity, String currentLocation) {
        this.activity = activity;
        this.defaultTextColor = getThemeTextColorPrimary(this.activity);
        this.currentLocation = currentLocation;

        this.imageType = SymphonyApplication.getInstance().getPreferenceUtils().getImageType();
        if (activity != null) {
            pixels = (int) (40 * activity.getResources().getDisplayMetrics().density);
        }
    }

    @NonNull
    @Override
    public FileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_manager_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileHolder holder, int position) {
        try {
            if (position < fileList.directories.size()) {
                holder.icon.setImageResource(R.drawable.ic_folder_black_24dp);
                holder.title.setText(fileList.directories.get(position).toString());
                setColorFilter(defaultTextColor, holder.icon, holder.menu);
            } else {
                String path = currentLocation + File.separator + fileList.files.get(position - fileList.directories.size()).toString();

                RequestOptions requestOptions = new RequestOptions();
                if (imageType == 0) {
                    requestOptions = requestOptions.transforms(new CenterCrop(), new CircleCrop());
                } else if (imageType == 1) {
                    requestOptions = requestOptions.transforms(new CenterCrop());
                } else {
                    requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(24));
                }

                GlideApp.with(activity)
                        .load(new Song(
                                -1, null, null, -1, path, null, 0, 0, -1, -1, -1
                        ))
                        .override(pixels, pixels)
                        .centerCrop()
                        .fitCenter()
                        .apply(requestOptions)
                        .transition(withCrossFade())
                        .error(
                                GlideApp
                                        .with(activity)
                                        .load(R.drawable.ic_blank_album_art)
                                        .override(pixels, pixels)
                                        .centerCrop()
                                        .fitCenter()
                                        .apply(requestOptions)
                                        .transition(withCrossFade())
                        )
                        .into(holder.icon);

                holder.title.setText(fileList.files.get(position - fileList.directories.size()).toString());
                setColorFilter(defaultTextColor, holder.menu);
                holder.icon.clearColorFilter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewRecycled(@NonNull FileManagerAdapter.FileHolder holder) {
        try {
            GlideApp.with(activity).clear(holder.icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return fileList != null ? fileList.directories.size() + fileList.files.size() : 0;
    }

    public void setFileList(FileList fileList, String currentLocation) {
        this.fileList = fileList;
        this.currentLocation = currentLocation;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    class FileHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title;
        ImageButton menu;

        FileHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.fileIcon);
            title = itemView.findViewById(R.id.fileName);
            menu = itemView.findViewById(R.id.menu);

            (itemView.findViewById(R.id.itemBackground)).setOnClickListener(view -> {
                if (callback != null) {
                    int position = getAdapterPosition();
                    if (position < fileList.directories.size()) {
                        callback.onItemClick(fileList.directories.get(position));
                    } else {
                        callback.onItemClick(fileList.files.get(position - fileList.directories.size()));
                    }
                }
            });

            menu.setOnClickListener(view -> {
                if (activity == null) {
                    return;
                }
                if (getAdapterPosition() < fileList.directories.size()) {
                    final ArrayList<Song> songs = getAllSongsOfFolder(activity.getContentResolver(), MediaStore.Audio.Media.TITLE, currentLocation + File.separator + fileList.directories.get(getAdapterPosition()).toString());
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_folders, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.action_play: {
                                if (songs.size() == 0) {
                                    postToast(R.string.error_label, activity, TOAST_ERROR);
                                } else {
                                    playList(songs, 0);
                                }
                                break;
                            }
                            case R.id.action_add_to_queue: {
                                if (songs.size() == 0) {
                                    postToast(R.string.error_label, activity, TOAST_ERROR);
                                } else {
                                    addListToQueue(songs);
                                }
                                break;
                            }
                            case R.id.action_add_to_playlist: {
                                if (songs.size() == 0) {
                                    postToast(R.string.error_label, activity, TOAST_ERROR);
                                } else {
                                    showAddToPlaylistDialog(activity, songs);
                                }
                                break;
                            }
                            case R.id.action_add_to_blacklist: {
                                BlacklistStore.getInstance(activity).addPath(new File(currentLocation + File.separator + fileList.directories.get(getAdapterPosition()).toString()));
                                postToast(R.string.added_to_blacklist, activity, TOAST_SUCCESS);
                                Intent intent = new Intent(RELOAD_LIBRARY_INTENT);
                                activity.sendBroadcast(intent);
                                break;
                            }
                        }
                        return true;
                    });
                    popupMenu.show();
                } else {
                    String path = currentLocation + File.separator + fileList.files.get(getAdapterPosition() - fileList.directories.size()).toString();
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_folder_songs, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(item -> {
                        final Song song = getSongFromPath(activity.getContentResolver(), path);
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
                                intent.putExtra("PATH", song.getPath());
                                intent.putExtra("ID", song.getId());
                                intent.putExtra("AlBUM_ID", song.getAlbumId());
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
                        }
                        return true;
                    });
                    popupMenu.show();
                }
            });
        }
    }

    public interface Callback {
        void onItemClick(FileList.FileWrapper file);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        activity = null;
    }
}