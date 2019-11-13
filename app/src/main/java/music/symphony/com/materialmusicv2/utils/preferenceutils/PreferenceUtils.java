package music.symphony.com.materialmusicv2.utils.preferenceutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.MediaStore;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Objects;

import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.Song;

public class PreferenceUtils {

    private SharedPreferences sharedPreferences;
    private Context context;

    public PreferenceUtils(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void checkNull() {
        if (context == null) {
            return;
        }
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setRepeat(int repeat) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putInt("repeat", repeat).apply();
        }
    }

    public int getRepeat() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("repeat", 0);
        }
        return 0;
    }

    public void setShuffle(boolean shuffle) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean("shuffle", shuffle).apply();
        }
    }

    public boolean getShuffle() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("shuffle", false);
        }
        return false;
    }

    public void modifySongPositionAndLastPlayed(final int songPosition, final Song currentSong) {
        if (sharedPreferences == null) {
            return;
        }
        AsyncTask.execute(() -> {
            try {
                sharedPreferences.edit().putInt("songPosition", songPosition).apply();
                if (currentSong == null) {
                    return;
                }
                Gson gson = new Gson();
                String getterJsonLastPlayed = sharedPreferences.getString("lastPlayed", null);
                ArrayList<Song> lastPlayed;
                if (getterJsonLastPlayed != null) {
                    Type type = new TypeToken<ArrayList<Song>>() {
                    }.getType();
                    lastPlayed = gson.fromJson(getterJsonLastPlayed, type);
                } else {
                    lastPlayed = new ArrayList<>();
                }
                lastPlayed.remove(currentSong);
                lastPlayed.add(0, currentSong);
                String setterJsonLastPlayed = gson.toJson(lastPlayed);
                sharedPreferences.edit().putString("lastPlayed", setterJsonLastPlayed).apply();
                String getterJsonMostPlayed = sharedPreferences.getString("mostPlayed", null);
                String getterJsonMostPlayedWeight = sharedPreferences.getString("mostPlayedWeight", null);
                ArrayList<Song> mostPlayed;
                ArrayList<Integer> mostPlayedWeight;
                if (getterJsonMostPlayed != null) {
                    Type type = new TypeToken<ArrayList<Song>>() {
                    }.getType();
                    mostPlayed = gson.fromJson(getterJsonMostPlayed, type);
                } else {
                    mostPlayed = new ArrayList<>();
                }
                if (getterJsonMostPlayedWeight != null) {
                    Type type = new TypeToken<ArrayList<Integer>>() {
                    }.getType();
                    mostPlayedWeight = gson.fromJson(getterJsonMostPlayedWeight, type);
                } else {
                    mostPlayedWeight = new ArrayList<>();
                }
                int weight = 0;
                int removePosition = -1;
                for (int i = 0; i < mostPlayed.size(); i++) {
                    weight = mostPlayedWeight.get(i);
                    Song song = mostPlayed.get(i);
                    if (currentSong.getPath().equals(song.getPath())) {
                        removePosition = i;
                        break;
                    }
                }
                if (removePosition != -1) {
                    mostPlayed.remove(removePosition);
                    mostPlayedWeight.remove(removePosition);
                    weight++;
                    for (int i = 0; i < mostPlayedWeight.size(); i++) {
                        if (weight >= mostPlayedWeight.get(i)) {
                            mostPlayed.add(i, currentSong);
                            mostPlayedWeight.add(i, weight);
                            break;
                        }
                    }
                } else {
                    mostPlayed.add(currentSong);
                    mostPlayedWeight.add(1);
                }
                String setterJsonMostPlayed = gson.toJson(mostPlayed);
                String setterJsonMostPlayedWeight = gson.toJson(mostPlayedWeight);
                sharedPreferences.edit().putString("mostPlayed", setterJsonMostPlayed).apply();
                sharedPreferences.edit().putString("mostPlayedWeight", setterJsonMostPlayedWeight).apply();
            } catch (Exception e) {
                sharedPreferences.edit().putString("mostPlayed", null).apply();
                sharedPreferences.edit().putString("mostPlayedWeight", null).apply();
                e.printStackTrace();
            }
        });
    }

    public int getSongPosition() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("songPosition", 0);
        }
        return 0;
    }

    public void readSongs() {
        if (sharedPreferences == null) {
            return;
        }
        Gson gson = new Gson();
        String songsJson = sharedPreferences.getString("songs", null);
        String songsWithoutShuffleJson = sharedPreferences.getString("songsWithoutShuffle", null);
        if (songsJson != null && songsWithoutShuffleJson != null) {
            Type type = new TypeToken<ArrayList<Song>>() {
            }.getType();
            ArrayList<Song> songs = gson.fromJson(songsJson, type);
            ArrayList<Song> songsWithoutShuffle = gson.fromJson(songsWithoutShuffleJson, type);
            if (songs.size() - 1 < SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition() || songsWithoutShuffle.size() - 1 < SymphonyApplication.getInstance().getPlayingQueueManager().getSongPosition()) {
                sharedPreferences.edit().remove("songPosition").apply();
                sharedPreferences.edit().remove("songs").apply();
                sharedPreferences.edit().remove("songsWithoutShuffle").apply();
                songs.clear();
                songsWithoutShuffle.clear();
            } else {
                SymphonyApplication.getInstance().getPlayingQueueManager().setSongs(songs);
                SymphonyApplication.getInstance().getPlayingQueueManager().setSongsWithoutShuffle(songsWithoutShuffle);
                songs.clear();
                songsWithoutShuffle.clear();
            }
        }
    }

    public void setSongs() {
        if (sharedPreferences == null) {
            return;
        }
        AsyncTask.execute(() -> {
            try {
                Gson gson = new Gson();
                String songsJson = gson.toJson(SymphonyApplication.getInstance().getPlayingQueueManager().getSongs());
                String songsWithoutShuffleJson = gson.toJson(SymphonyApplication.getInstance().getPlayingQueueManager().getSongsWithoutShuffle());
                sharedPreferences.edit().putString("songs", songsJson).apply();
                sharedPreferences.edit().putString("songsWithoutShuffle", songsWithoutShuffleJson).apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public boolean getPauseOnDisconnect() {
        return sharedPreferences == null || sharedPreferences.getBoolean("pauseOnDisconnect", true);
    }

    public boolean getFade() {
        return sharedPreferences != null && sharedPreferences.getBoolean("fade", false);
    }

    public boolean getUseMediaStyleNotification() {
        return sharedPreferences == null || sharedPreferences.getBoolean("useMediaStyleNotification", true);
    }

    public int getVisualizerType() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("visualizerType", 1);
        } else {
            return 1;
        }
    }

    public void setVisualizerType(int visualizerType) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("visualizerType", visualizerType).apply();
    }

    public int getDefaultPage() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("defaultPage", 0);
        } else {
            return 0;
        }
    }

    public void setDefaultPage(int defaultPage) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("defaultPage", defaultPage).apply();
    }

    public void setAlbumGrid(int albumGrid) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("albumGrid", albumGrid).apply();
    }

    public int getAlbumGrid() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("albumGrid", 3);
        } else {
            return 3;
        }
    }

    public void setArtistGrid(int artistGrid) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("artistGrid", artistGrid).apply();
    }

    public int getArtistGrid() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("artistGrid", 3);
        } else {
            return 3;
        }
    }

    public void setTheme(int theme) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("theme", theme).apply();
    }

    public int getTheme() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("theme", 0);
        } else {
            return 0;
        }
    }

    public void setAccentColor(int accentColor) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("accentColor", accentColor).apply();
    }

    public int getAccentColor() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("accentColor", 34);
        } else {
            return 0;
        }
    }

    public boolean getDonated() {
        return sharedPreferences == null || sharedPreferences.getBoolean("donated", false);
    }

    public void setDonated(boolean donated) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putBoolean("donated", donated).apply();
    }

    public int getLanguage() {
        if (sharedPreferences != null) {
            return Objects.equals(sharedPreferences.getString("language", "0"), "0") ? 0 : 1;
        } else {
            return 0;
        }
    }

    public void setLanguage(int language) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putString("language", String.valueOf(language)).apply();
    }

    public int getNowPlayingStyle() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("nowPlayingStyle", 1);
        } else {
            return 1;
        }
    }

    public void setNowPlayingStyle(int nowPlayingStyle) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("nowPlayingStyle", nowPlayingStyle).apply();
    }

    public boolean getInitializedBlacklist() {
        return sharedPreferences != null && sharedPreferences.getBoolean("initializedBlacklist", false);
    }

    public void setInitializedBlacklist() {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putBoolean("initializedBlacklist", true).apply();
    }

    public boolean getShowQueueInNotification() {
        return sharedPreferences != null && sharedPreferences.getBoolean("showQueueInNotification", false);
    }

    public boolean getDownloadOnlyOnWifi() {
        return sharedPreferences != null && sharedPreferences.getBoolean("downloadOnlyOnWifi", true);
    }

    public void setEqualizerEnabled(boolean equalizerEnabled) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putBoolean("equalizerEnabled", equalizerEnabled).apply();
    }

    public boolean getEqualizerEnabled() {
        return sharedPreferences != null && sharedPreferences.getBoolean("equalizerEnabled", false);
    }

    public void setEqualizerBandLevel(int band, int level) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("equalizerBandLevel" + band, level).apply();
    }

    public int getEqualizerBandLevel(int band) {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("equalizerBandLevel" + band, -1);
        }
        return -1;
    }

    public int getPreset() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("reverb", -1);
        }
        return -1;
    }

    public void setPreset(int preset) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("reverb", preset).apply();
    }

    public int getBassBoostLevel() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("bassBoostLevel", 0);
        }
        return 0;
    }

    public void setBassBoostLevel(int bassboostLevel) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("bassBoostLevel", bassboostLevel).apply();
    }

    public int getVirtualizerLevel() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("virtualizerLevel", 0);
        }
        return 0;
    }

    public void setVirtualizerLevel(int virtualizerLevel) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("virtualizerLevel", virtualizerLevel).apply();
    }

    public int getPresetReverb() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("presetReverb", 0);
        }
        return 0;
    }

    public void setPresetReverb(int presetReverb) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("presetReverb", presetReverb).apply();
    }

    public boolean getIsIntroShown() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("isIntroShown", false);
        }
        return false;
    }

    public void setIsIntroShown(boolean isIntroShown) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putBoolean("isIntroShown", isIntroShown).apply();
    }

    public String getTreeUri() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString("treeUri", null);
        }
        return null;
    }

    public void setTreeUri(String treeUri) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putString("treeUri", treeUri).apply();
    }

    public boolean getColorizeElementsAccordingToAlbumArt() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("colorizeElementsAccordingToAlbumArt", true);
        }
        return true;
    }

    public int getVisualizerSpeed() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("visualizerSpeed", 1);
        }
        return 1;
    }

    public void setVisualizerSpeed(int visualizerSpeed) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("visualizerSpeed", visualizerSpeed).apply();
    }

    public int getTabTitlesMode() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("tabTitlesMode", 1);
        }
        return 1;
    }

    public void setTabTitlesMode(int tabTitlesMode) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("tabTitlesMode", tabTitlesMode).apply();
    }

    public boolean getMainScreenStyle() {
        if (sharedPreferences != null) {
            return !sharedPreferences.getBoolean("mainScreenStyle", false);
        }
        return false;
    }

    public int getAlbumItemStyle() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("albumItemStyle", 0);
        }
        return 0;
    }

    public void setAlbumItemStyle(int albumItemStyle) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("albumItemStyle", albumItemStyle).apply();
    }

    public int getArtistItemStyle() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("artistItemStyle", 0);
        }
        return 0;
    }

    public void setArtistItemStyle(int artistItemStyle) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("artistItemStyle", artistItemStyle).apply();
    }

    public void setImageType(int imageType) {
        if (sharedPreferences == null) {
            return;
        }
        sharedPreferences.edit().putInt("imageType", imageType).apply();
    }

    public int getImageType() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("imageType", 0);
        }
        return 0;
    }

    public String getSongSortOrder() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString("songSortOrder", MediaStore.Audio.Media.TITLE);
        }
        return MediaStore.Audio.Media.TITLE;
    }

    public void setSongSortOrder(String sortOrder) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString("songSortOrder", sortOrder).apply();
        }
    }

    public String getAlbumSortOrder() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString("albumSortOrder", MediaStore.Audio.Media.TITLE);
        }
        return MediaStore.Audio.Media.TITLE;
    }

    public void setAlbumSortOrder(String sortOrder) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString("albumSortOrder", sortOrder).apply();
        }
    }

    public int getLastOpenedFragment() {
        if (sharedPreferences != null) {
            return sharedPreferences.getInt("lastOpenedFragment", 0);
        }
        return 0;
    }

    public void setLastOpenedFragment(int lastOpenedFragment) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putInt("lastOpenedFragment", lastOpenedFragment).apply();
        }
    }

    public boolean getEnableVisualizer() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean("enableVisualizer", true);
        }
        return true;
    }
}
