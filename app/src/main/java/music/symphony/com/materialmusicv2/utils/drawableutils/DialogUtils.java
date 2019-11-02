package music.symphony.com.materialmusicv2.utils.drawableutils;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import java.util.ArrayList;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomColorChooserSheetFragment;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomFileChooserSheetFragment;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomInputSheetFragment;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomSeekbarInputSheetFragment;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomSelectorSheetFragment;
import music.symphony.com.materialmusicv2.bottomsheetdialogs.BottomYesNoSheetFragment;
import music.symphony.com.materialmusicv2.objects.Playlist;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.themeutils.ThemeUtils;

import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_ERROR;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_INFO;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;
import static music.symphony.com.materialmusicv2.utils.misc.Statics.RELOAD_LIBRARY_INTENT;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.addListToPlaylist;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.addToPlaylist;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.createPlaylist;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getAllPlaylists;
import static music.symphony.com.materialmusicv2.utils.queryutils.QueryUtils.getPlaylistID;

public class DialogUtils {

    public interface OnSelectedListener {
        void onSelected(int position);
    }

    public static void showSelectorDialog(Activity activity, int title, int items, int selectedPosition, OnSelectedListener onSelectedListener) {
        BottomSelectorSheetFragment sheet = new BottomSelectorSheetFragment(activity);
        sheet.setCallback(new BottomSelectorSheetFragment.Callback() {
            @Override
            public void listItemSelected(int position) {
                if (onSelectedListener != null) {
                    onSelectedListener.onSelected(position);
                }
            }

            @Override
            public void onNoSelected() {
            }
        });
        sheet.setTitle(title);
        sheet.setNoText(R.string.cancel_label);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setTextColor(ThemeUtils.getThemeTextColorPrimary(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.setSelectedItem(selectedPosition);
        sheet.setList(items);
        sheet.show();
    }

    public static void showColorChooserDialog(Activity activity, int title, BottomColorChooserSheetFragment.Callback callback) {
        BottomColorChooserSheetFragment sheet = new BottomColorChooserSheetFragment(activity);
        sheet.setTitle(title);
        sheet.setYesText(R.string.yes);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.setCallback(callback);
        sheet.show();
    }

    public static void showFileChooserDialog(Activity activity, int title, BottomFileChooserSheetFragment.Callback callback) {
        BottomFileChooserSheetFragment sheet = new BottomFileChooserSheetFragment(activity);
        sheet.setYesText(R.string.yes);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.setCallback(callback);
        sheet.show();
    }

    public interface OnInputListener {
        void onInput(String input);
    }

    public static void showInputDialog(Activity activity, int title, int hint, OnInputListener onInputListener) {
        BottomInputSheetFragment sheet = new BottomInputSheetFragment(activity);
        sheet.setTitle(title);
        sheet.setYesText(R.string.yes);
        sheet.setNoText(R.string.cancel_label);
        sheet.setHint(hint);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setTextColor(ThemeUtils.getThemeTextColorPrimary(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.setCallback(new BottomInputSheetFragment.Callback() {
            @Override
            public void onYesSelected(String input) {
                if (onInputListener != null) {
                    onInputListener.onInput(input);
                }
            }

            @Override
            public void onNoSelected() {
            }
        });
        sheet.show();
    }

    public static void showSeekbarInputDialog(Activity activity, int title, int message, int minInput, int maxInput, OnInputListener onInputListener) {
        BottomSeekbarInputSheetFragment sheet = new BottomSeekbarInputSheetFragment(activity);
        sheet.setTitle(title);
        sheet.setYesText(R.string.yes);
        sheet.setNoText(R.string.cancel_label);
        sheet.setMessage(message);
        sheet.setMinAndMax(minInput, maxInput);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setTextColor(ThemeUtils.getThemeTextColorPrimary(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.setCallback(new BottomSeekbarInputSheetFragment.Callback() {
            @Override
            public void onYesSelected(String input) {
                if (onInputListener != null) {
                    onInputListener.onInput(input);
                }
            }

            @Override
            public void onNoSelected() {
            }
        });
        sheet.show();
    }

    public interface OnYesNoSelectedListener {
        void onYesSelected();

        void onNoSelected();
    }

    public static void showYesNoDialog(Activity activity, int title, int content, OnYesNoSelectedListener onYesNoSelectedListener) {
        BottomYesNoSheetFragment sheet = new BottomYesNoSheetFragment(activity);
        sheet.setCallback(new BottomYesNoSheetFragment.Callback() {
            @Override
            public void onYesSelected() {
                if (onYesNoSelectedListener != null) {
                    onYesNoSelectedListener.onYesSelected();
                }
            }

            @Override
            public void onNoSelected() {
                if (onYesNoSelectedListener != null) {
                    onYesNoSelectedListener.onNoSelected();
                }
            }
        });
        sheet.setTitle(title);
        sheet.setMessage(content);
        sheet.setYesText(R.string.yes);
        sheet.setNoText(R.string.no);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setTextColor(ThemeUtils.getThemeTextColorPrimary(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.show();
    }

    public static void showAddToPlaylistDialog(Activity activity, Song song, boolean isFavoritesAvailable) {
        if (activity == null) {
            return;
        }
        final ArrayList<Playlist> playlists = getAllPlaylists(activity.getContentResolver(), MediaStore.Audio.Playlists.NAME, false);
        if (playlists == null) {
            return;
        }
        int position = -1;
        if (!isFavoritesAvailable) {
            for (Playlist currentPlaylist : playlists) {
                if (currentPlaylist.getName().equals("Favorites")) {
                    position = playlists.indexOf(currentPlaylist);
                    break;
                }
            }
        }
        if (position != -1) {
            playlists.remove(position);
        }
        ArrayList<String> list = new ArrayList<>();
        list.add(activity.getString(R.string.create_new_playlist));
        for (int i = 0; i < playlists.size(); i++) {
            list.add(playlists.get(i).getName());
        }
        BottomSelectorSheetFragment sheet = new BottomSelectorSheetFragment(activity);
        sheet.setCallback(new BottomSelectorSheetFragment.Callback() {
            @Override
            public void listItemSelected(int position) {
                if (position > 0) {
                    AsyncTask.execute(() -> addToPlaylist(activity.getContentResolver(), (int) song.getId(), playlists.get(position - 1).getId()));
                    postToast(String.format(activity.getString(R.string.add_to_playlist_toast), 1), activity, TOAST_SUCCESS);
                } else {
                    showInputDialog(activity,
                            R.string.create_playlist,
                            R.string.enter_playlist_name,
                            input -> {
                                int isPlaylistCreated = createPlaylist(input, activity);
                                if (isPlaylistCreated == 1) {
                                    long playlistID = getPlaylistID(input, activity);
                                    postToast(R.string.playlist_created, activity, TOAST_SUCCESS);
                                    AsyncTask.execute(() -> addToPlaylist(activity.getContentResolver(), (int) song.getId(), playlistID));
                                    postToast(String.format(activity.getString(R.string.add_to_playlist_toast), 1), activity, TOAST_SUCCESS);
                                    Intent intent = new Intent(RELOAD_LIBRARY_INTENT);
                                    activity.sendBroadcast(intent);
                                } else if (isPlaylistCreated == 0) {
                                    postToast(R.string.playlist_exists, activity, TOAST_INFO);
                                } else {
                                    postToast(R.string.error_label, activity, TOAST_ERROR);
                                }
                            });
                }
            }

            @Override
            public void onNoSelected() {
            }
        });
        sheet.setTitle(R.string.add_to_playlist_instruction);
        sheet.setNoText(R.string.cancel_label);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setTextColor(ThemeUtils.getThemeTextColorPrimary(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.setList(list);
        sheet.show();
    }

    public static void showAddToPlaylistDialog(Activity activity, ArrayList<Song> songs) {
        if (activity == null) {
            return;
        }
        final ArrayList<Playlist> playlists = getAllPlaylists(activity.getContentResolver(), MediaStore.Audio.Playlists.NAME, false);
        if (playlists == null) {
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        list.add(activity.getString(R.string.create_new_playlist));
        for (int i = 0; i < playlists.size(); i++) {
            list.add(playlists.get(i).getName());
        }
        BottomSelectorSheetFragment sheet = new BottomSelectorSheetFragment(activity);
        sheet.setCallback(new BottomSelectorSheetFragment.Callback() {
            @Override
            public void listItemSelected(int position) {
                if (position > 0) {
                    AsyncTask.execute(() -> {
                        addListToPlaylist(activity.getContentResolver(), songs, playlists.get(position - 1).getId());
                        new Handler(Looper.getMainLooper()).post(() -> postToast(String.format(activity.getString(R.string.add_to_playlist_toast), songs.size()), activity, TOAST_SUCCESS));
                    });
                } else {
                    showInputDialog(activity,
                            R.string.create_playlist,
                            R.string.enter_playlist_name,
                            input -> AsyncTask.execute(() -> {
                                int isPlaylistCreated = createPlaylist(input, activity);
                                if (isPlaylistCreated == 1) {
                                    long playlistID = getPlaylistID(input, activity);
                                    new Handler(Looper.getMainLooper()).post(() -> postToast(R.string.playlist_created, activity, TOAST_SUCCESS));
                                    addListToPlaylist(activity.getContentResolver(), songs, playlistID);
                                    new Handler(Looper.getMainLooper()).post(() -> postToast(String.format(activity.getString(R.string.add_to_playlist_toast), songs.size()), activity, TOAST_SUCCESS));
                                    Intent intent = new Intent(RELOAD_LIBRARY_INTENT);
                                    activity.sendBroadcast(intent);
                                } else if (isPlaylistCreated == 0) {
                                    new Handler(Looper.getMainLooper()).post(() -> postToast(R.string.playlist_exists, activity, TOAST_INFO));
                                } else {
                                    new Handler(Looper.getMainLooper()).post(() -> postToast(R.string.error_label, activity, TOAST_ERROR));
                                }
                            }));
                }
            }

            @Override
            public void onNoSelected() {
            }
        });
        sheet.setTitle(R.string.add_to_playlist_instruction);
        sheet.setNoText(R.string.cancel_label);
        sheet.setAccentColor(ThemeUtils.getThemeAccentColor(activity));
        sheet.setTextColor(ThemeUtils.getThemeTextColorPrimary(activity));
        sheet.setBackgroundColor(ThemeUtils.getThemeWindowBackgroundColor(activity));
        sheet.setList(list);
        sheet.show();
    }
}
