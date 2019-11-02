package music.symphony.com.materialmusicv2.service;

import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;

import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.objects.events.SongListChanged;
import music.symphony.com.materialmusicv2.objects.events.SongPositionInQueue;

public class PlayingQueueManager {

    private static final String TAG = "playingqueuemanager";

    /**
     * Songs
     */

    private ArrayList<Song> songs = null;
    private ArrayList<Song> songsWithoutShuffle = null;

    public void setSongs(ArrayList<Song> songs) {
        this.songs = new ArrayList<>(songs);
        this.songsWithoutShuffle = new ArrayList<>(songs);
        EventBus.getDefault().removeStickyEvent(SongListChanged.class);
        EventBus.getDefault().postSticky(new SongListChanged(this.songs));
    }

    public void setSongsWithoutShuffle(ArrayList<Song> songsWithoutShuffle) {
        this.songsWithoutShuffle = new ArrayList<>(songsWithoutShuffle);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public ArrayList<Song> getSongsWithoutShuffle() {
        return songsWithoutShuffle;
    }

    public int getNumberOfSongsInQueue() {
        if (songs != null) {
            return songs.size();
        }
        return 0;
    }

    public void remove(int position) {
        try {
            if (songs != null && songsWithoutShuffle != null) {
                if (getSongPosition() == getNumberOfSongsInQueue() - 1) {
                    setSongPosition(0);
                }
                Song currentSong = getCurrentSong();
                Song song = songs.remove(position);
                if (song != null) {
                    songsWithoutShuffle.remove(song);
                }
                setSongPosition(songs.indexOf(currentSong));
                EventBus.getDefault().removeStickyEvent(SongListChanged.class);
                EventBus.getDefault().postSticky(new SongListChanged(songs));
                EventBus.getDefault().removeStickyEvent(SongPositionInQueue.class);
                EventBus.getDefault().postSticky(new SongPositionInQueue(songPosition));
            }
        } catch (Exception e) {
            Log.v(TAG, "index doesn't exist in songs");
        }
    }

    void move(int from, int to) {
        try {
            Song song = getCurrentSong();
            if (songs != null) {
                songs.add(to, songs.remove(from));
                songPosition = songs.indexOf(song);
            }
            EventBus.getDefault().removeStickyEvent(SongListChanged.class);
            EventBus.getDefault().postSticky(new SongListChanged(songs));
            EventBus.getDefault().removeStickyEvent(SongPositionInQueue.class);
            EventBus.getDefault().postSticky(new SongPositionInQueue(songPosition));
        } catch (Exception e) {
            Log.v(TAG, "index doesn't exist in songs");
        }
    }

    public void addToQueue(Song song) {
        if (songs != null) {
            songs.add(song);
        }
        if (songsWithoutShuffle != null) {
            songsWithoutShuffle.add(song);
        }
        EventBus.getDefault().removeStickyEvent(SongListChanged.class);
        EventBus.getDefault().postSticky(new SongListChanged(songs));
    }

    public void addToQueue(ArrayList<Song> songs) {
        if (this.songs != null) {
            this.songs.addAll(songs);
        }
        if (songsWithoutShuffle != null) {
            songsWithoutShuffle.addAll(songs);
        }
        EventBus.getDefault().removeStickyEvent(SongListChanged.class);
        EventBus.getDefault().postSticky(new SongListChanged(this.songs));
    }

    public void addToNextPosition(Song song) {
        if (songs != null) {
            songs.add(getSongPosition() + 1, song);
        }
        if (songsWithoutShuffle != null) {
            songsWithoutShuffle.add(song);
        }

        EventBus.getDefault().removeStickyEvent(SongListChanged.class);
        EventBus.getDefault().postSticky(new SongListChanged(songs));
    }

    /**
     * Song position
     */

    private int songPosition = 0;

    public void setSongPosition(int songPosition) {
        this.songPosition = songPosition;
    }

    public int getSongPosition() {
        return songPosition;
    }

    public Song getCurrentSong() {
        try {
            if (songs != null) {
                return songs.get(songPosition);
            }
        } catch (Exception e) {
            Log.v(TAG, "index doesn't exist in songs");
        }
        return null;
    }

    /**
     * Repeat
     */

    private int repeat = PlaybackStateCompat.REPEAT_MODE_NONE;

    public void setRepeat(int repeat) {
        this.repeat = repeat;
        try {
            SymphonyApplication.getInstance().getPreferenceUtils().setRepeat(repeat);
        } catch (Exception e) {
            Log.v(TAG, "PreferenceUtil is null!");
        }
    }

    void changeRepeat() {
        if (repeat == PlaybackStateCompat.REPEAT_MODE_NONE) {
            repeat = PlaybackStateCompat.REPEAT_MODE_ALL;
        } else if (repeat == PlaybackStateCompat.REPEAT_MODE_ALL) {
            repeat = PlaybackStateCompat.REPEAT_MODE_ONE;
        } else if (repeat == PlaybackStateCompat.REPEAT_MODE_ONE) {
            repeat = PlaybackStateCompat.REPEAT_MODE_NONE;
        }
        try {
            SymphonyApplication.getInstance().getPreferenceUtils().setShuffle(shuffle);
        } catch (Exception e) {
            Log.v(TAG, "PreferenceUtil is null!");
        }
    }

    public int getRepeat() {
        return repeat;
    }

    /**
     * Shuffle
     */

    private boolean shuffle = false;

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
        try {
            SymphonyApplication.getInstance().getPreferenceUtils().setShuffle(shuffle);
        } catch (Exception e) {
            Log.v(TAG, "PreferenceUtil is null!");
        }
    }

    void changeShuffle() {
        shuffle = !shuffle;
        Song song = getCurrentSong();
        if (shuffle) {
            if (getNumberOfSongsInQueue() > 2) {
                Collections.shuffle(songs);
            }
        } else {
            songs = new ArrayList<>(songsWithoutShuffle);
        }
        try {
            SymphonyApplication.getInstance().getPreferenceUtils().setShuffle(shuffle);
        } catch (Exception e) {
            Log.v(TAG, "PreferenceUtil is null!");
        }
        setSongPosition(songs.indexOf(song));
        EventBus.getDefault().removeStickyEvent(SongListChanged.class);
        EventBus.getDefault().postSticky(new SongListChanged(songs));
        EventBus.getDefault().removeStickyEvent(SongPositionInQueue.class);
        EventBus.getDefault().postSticky(new SongPositionInQueue(getSongPosition()));
    }

    public boolean getShuffle() {
        return shuffle;
    }
}
