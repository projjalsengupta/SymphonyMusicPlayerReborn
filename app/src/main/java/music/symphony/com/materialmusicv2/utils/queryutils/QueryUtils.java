package music.symphony.com.materialmusicv2.utils.queryutils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.objects.Album;
import music.symphony.com.materialmusicv2.objects.Artist;
import music.symphony.com.materialmusicv2.objects.Genre;
import music.symphony.com.materialmusicv2.objects.MostPlayedList;
import music.symphony.com.materialmusicv2.objects.Playlist;
import music.symphony.com.materialmusicv2.objects.Song;
import music.symphony.com.materialmusicv2.utils.blacklist.BlacklistStore;

import static music.symphony.com.materialmusicv2.utils.misc.Etc.TOAST_SUCCESS;
import static music.symphony.com.materialmusicv2.utils.misc.Etc.postToast;

public class QueryUtils {

    private static final String BASE_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1" + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static ArrayList<Song> getAllSongs(ContentResolver contentResolver, String sortOrder) {
        String selection = BASE_SELECTION;
        String[] selectionValues = new String[0];
        ArrayList<String> paths = BlacklistStore.getInstance(SymphonyApplication.getInstance().getApplicationContext()).getPaths();
        if (!paths.isEmpty()) {
            selection = generateBlacklistSelection(selection, paths.size());
            selectionValues = addBlacklistSelectionValues(selectionValues, paths);
        }
        ArrayList<Song> songs = new ArrayList<>();
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, selection, selectionValues, sortOrder, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int artColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    String thisArtist = cursor.getString(artistColumn);
                    long thisArt = cursor.getLong(artColumn);
                    String thisData = cursor.getString(dataColumn);
                    String thisAlbum = cursor.getString(albumColumn);
                    int duration = cursor.getInt(durationColumn);
                    int thisDate = cursor.getInt(dateColumn);
                    long artistId = cursor.getLong(artistIdColumn);
                    int trackNumber = cursor.getInt(trackNumberColumn);
                    int year = cursor.getInt(yearColumn);
                    songs.add(new Song(thisId, thisTitle, thisArtist, thisArt, thisData, thisAlbum, thisDate, duration, artistId, trackNumber, year));
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public static Song getSongFromPath(ContentResolver contentResolver, String path) {
        String selection = BASE_SELECTION + "AND " + MediaStore.Audio.AudioColumns.DATA + " LIKE ?";
        String[] selectionValues = new String[1];
        selectionValues[0] = path;

        Song song = null;
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, selection, selectionValues, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int artColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    String thisArtist = cursor.getString(artistColumn);
                    long thisArt = cursor.getLong(artColumn);
                    String thisData = cursor.getString(dataColumn);
                    String thisAlbum = cursor.getString(albumColumn);
                    int duration = cursor.getInt(durationColumn);
                    int thisDate = cursor.getInt(dateColumn);
                    long artistId = cursor.getLong(artistIdColumn);
                    int trackNumber = cursor.getInt(trackNumberColumn);
                    int year = cursor.getInt(yearColumn);

                    song = new Song(thisId, thisTitle, thisArtist, thisArt, thisData, thisAlbum, thisDate, duration, artistId, trackNumber, year);
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return song;
    }

    public static ArrayList<Album> getAllAlbums(ContentResolver contentResolver) {
        ArrayList<Song> songs = getAllSongs(contentResolver, MediaStore.Audio.Media.TITLE);
        ArrayList<Album> albums = splitIntoAlbums(songs);
        sortAlbumsByName(albums);
        return albums;
    }

    private static ArrayList<Album> splitIntoAlbums(final ArrayList<Song> songs) {
        ArrayList<Album> albums = new ArrayList<>();
        if (songs != null) {
            for (Song song : songs) {
                getOrCreateAlbum(albums, (int) song.getAlbumId()).getSongs().add(song);
            }
        }
        for (Album album : albums) {
            sortSongsByTrackNumber(album.getSongs());
        }
        return albums;
    }

    private static Album getOrCreateAlbum(ArrayList<Album> albums, int albumId) {
        for (Album album : albums) {
            if (!album.getSongs().isEmpty() && album.getSongs().get(0).getAlbumId() == albumId) {
                return album;
            }
        }
        Album album = new Album();
        albums.add(album);
        return album;
    }

    private static void sortSongsByTrackNumber(ArrayList<Song> songs) {
        Collections.sort(songs, (o1, o2) -> o1.getTrackNumber() - o2.getTrackNumber());
    }

    private static void sortAlbumsByName(ArrayList<Album> albums) {
        Collections.sort(albums, (Album o1, Album o2) -> o1.getName().compareTo(o2.getName()));
    }

    private static void sortArtistsByName(ArrayList<Artist> artists) {
        Collections.sort(artists, (Artist o1, Artist o2) -> o1.getName().compareTo(o2.getName()));
    }

    public static ArrayList<Artist> getAllArtists(ContentResolver contentResolver) {
        ArrayList<Song> songs = getAllSongs(contentResolver, MediaStore.Audio.Media.TITLE);
        ArrayList<Artist> artists = splitIntoArtists(splitIntoAlbums(songs));
        sortArtistsByName(artists);
        return artists;
    }

    private static ArrayList<Artist> splitIntoArtists(final ArrayList<Album> albums) {
        ArrayList<Artist> artists = new ArrayList<>();
        if (albums != null) {
            for (Album album : albums) {
                getOrCreateArtist(artists, album.getArtistId()).albums.add(album);
            }
        }
        return artists;
    }

    private static Artist getOrCreateArtist(ArrayList<Artist> artists, int artistId) {
        for (Artist artist : artists) {
            if (!artist.albums.isEmpty() && !artist.albums.get(0).getSongs().isEmpty() && artist.albums.get(0).getSongs().get(0).getArtistId() == artistId) {
                return artist;
            }
        }
        Artist artist = new Artist();
        artists.add(artist);
        return artist;
    }

    public static ArrayList<Genre> getAllGenres(ContentResolver contentResolver, String sortOrder) {
        ArrayList<Genre> genres = new ArrayList<>();
        try {
            Uri uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Genres.NAME);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Genres._ID);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    if (!thisTitle.trim().equals("")) {
                        genres.add(new Genre(thisId, thisTitle));
                    }
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genres;
    }

    public static ArrayList<Playlist> getAllPlaylists(ContentResolver contentResolver, String sortOrder, boolean isNeeded) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        try {
            Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                int playlistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Playlists.NAME);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Playlists._ID);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisPlaylist = cursor.getString(playlistColumn);
                    playlists.add(new Playlist(thisId, thisPlaylist));
                }
                while (cursor.moveToNext());
                cursor.close();
                if (isNeeded) {
                    playlists.add(0, new Playlist(-1, "Last Added"));
                    playlists.add(0, new Playlist(-2, "Recently Played"));
                    playlists.add(0, new Playlist(-3, "Most Played"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playlists;
    }

    public static ArrayList<Song> getSongsOfAlbum(long albumID, ContentResolver contentResolver) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            String selection = "is_music != 0";
            if (albumID > 0) {
                selection = selection + " and album_id = " + albumID;
            }
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.YEAR
            };
            final String sortOrder = MediaStore.Audio.AudioColumns.TRACK + " COLLATE LOCALIZED ASC";
            Cursor cursor = null;
            try {
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                cursor = contentResolver.query(uri, projection, selection, null, sortOrder);
                if (cursor != null) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        long thisId = cursor.getLong(0);
                        String thisName = cursor.getString(1);
                        String thisArtist = cursor.getString(2);
                        long thisArt = cursor.getLong(3);
                        String thisData = cursor.getString(4);
                        String thisAlbum = cursor.getString(5);
                        int thisDate = cursor.getInt(6);
                        int thisDuration = cursor.getInt(7);
                        long artistId = cursor.getLong(8);
                        int trackNumber = cursor.getInt(9);
                        int year = cursor.getInt(10);
                        songs.add(new Song(thisId, thisName, thisArtist, thisArt, thisData, thisAlbum, thisDate, thisDuration, artistId, trackNumber, year));
                        cursor.moveToNext();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public static ArrayList<Song> getAllSongsFromGenre(ContentResolver contentResolver, String sortOrder, long ID) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", ID);
            Cursor cursor = contentResolver.query(uri, null, null, null, sortOrder, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int artColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    String thisArtist = cursor.getString(artistColumn);
                    long thisArt = cursor.getLong(artColumn);
                    String thisData = cursor.getString(dataColumn);
                    String thisAlbum = cursor.getString(albumColumn);
                    int duration = cursor.getInt(durationColumn);
                    int thisDate = cursor.getInt(dateColumn);
                    long artistId = cursor.getInt(artistIdColumn);
                    int trackNumber = cursor.getInt(trackNumberColumn);
                    int year = cursor.getInt(yearColumn);
                    songs.add(new Song(thisId, thisTitle, thisArtist, thisArt, thisData, thisAlbum, thisDate, duration, artistId, trackNumber, year));
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public static ArrayList<Song> getLastPlayedSongs(Context context) {
        ArrayList<Song> songs = new ArrayList<>();
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString("lastPlayed", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Song>>() {
            }.getType();
            songs = gson.fromJson(json, type);
        }
        return songs;
    }

    public static ArrayList<Song> getMostPlayedSongs(Context context) {
        ArrayList<Song> songs = new ArrayList<>();
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString("mostPlayed", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Song>>() {
            }.getType();
            songs = gson.fromJson(json, type);
        }
        return songs;
    }

    public static ArrayList<Integer> getMostPlayedSongPlayCountList(Context context) {
        ArrayList<Integer> playCountList;
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString("mostPlayedWeight", null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Integer>>() {
            }.getType();
            playCountList = gson.fromJson(json, type);
        } else {
            playCountList = new ArrayList<>();
        }
        return playCountList;
    }

    public static ArrayList<Song> getAllSongsOfFolder(ContentResolver contentResolver, String sortOrder, String path) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            String selection = BASE_SELECTION;
            String[] selectionValues = new String[0];
            ArrayList<String> paths = BlacklistStore.getInstance(SymphonyApplication.getInstance().getApplicationContext()).getPaths();
            if (!paths.isEmpty()) {
                selection = generateBlacklistSelection(selection, paths.size());
                selectionValues = addBlacklistSelectionValues(selectionValues, paths);
            }
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, selection, selectionValues, sortOrder, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int artColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    String thisArtist = cursor.getString(artistColumn);
                    long thisArt = cursor.getLong(artColumn);
                    String thisData = cursor.getString(dataColumn);
                    String thisAlbum = cursor.getString(albumColumn);
                    int duration = cursor.getInt(durationColumn);
                    int thisDate = cursor.getInt(dateColumn);
                    long artistId = cursor.getInt(artistIdColumn);
                    int trackNumber = cursor.getInt(trackNumberColumn);
                    int year = cursor.getInt(yearColumn);
                    File file = new File(thisData);
                    if (file.getParentFile().getPath().equals(path)) {
                        songs.add(new Song(thisId, thisTitle, thisArtist, thisArt, thisData, thisAlbum, thisDate, duration, artistId, trackNumber, year));
                    }
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public static ArrayList<Song> getAllSongsOfFolderWithoutSelection(ContentResolver contentResolver, String sortOrder, String path) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, BASE_SELECTION, null, sortOrder, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int artColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    String thisArtist = cursor.getString(artistColumn);
                    long thisArt = cursor.getLong(artColumn);
                    String thisData = cursor.getString(dataColumn);
                    String thisAlbum = cursor.getString(albumColumn);
                    int duration = cursor.getInt(durationColumn);
                    int thisDate = cursor.getInt(dateColumn);
                    long artistId = cursor.getInt(artistIdColumn);
                    int trackNumber = cursor.getInt(trackNumberColumn);
                    int year = cursor.getInt(yearColumn);
                    File file = new File(thisData);
                    if (file.getParentFile().getPath().equals(path)) {
                        songs.add(new Song(thisId, thisTitle, thisArtist, thisArt, thisData, thisAlbum, thisDate, duration, artistId, trackNumber, year));
                    }
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public static ArrayList<Song> getAllSongsFromPlaylist(ContentResolver contentResolver, long ID) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            String selection = BASE_SELECTION;
            String[] selectionValues = new String[0];
            ArrayList<String> paths = BlacklistStore.getInstance(SymphonyApplication.getInstance().getApplicationContext()).getPaths();
            if (!paths.isEmpty()) {
                selection = generateBlacklistSelection(selection, paths.size());
                selectionValues = addBlacklistSelectionValues(selectionValues, paths);
            }
            String sortOrder = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", ID);
            Cursor cursor = contentResolver.query(uri, null, selection, selectionValues, sortOrder);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Playlists.Members.AUDIO_ID);
                int artistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int artColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    String thisArtist = cursor.getString(artistColumn);
                    long thisArt = cursor.getLong(artColumn);
                    String thisData = cursor.getString(dataColumn);
                    String thisAlbum = cursor.getString(albumColumn);
                    int thisDate = cursor.getInt(dateColumn);
                    int duration = cursor.getInt(durationColumn);
                    long artistId = cursor.getInt(artistIdColumn);
                    int trackNumber = cursor.getInt(trackNumberColumn);
                    int year = cursor.getInt(yearColumn);
                    songs.add(new Song(thisId, thisTitle, thisArtist, thisArt, thisData, thisAlbum, thisDate, duration, artistId, trackNumber, year));
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public static boolean movePlaylistItem(ContentResolver contentResolver, long playlistID, int fromPosition, int toPosition) {
        try {
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external",
                    playlistID)
                    .buildUpon()
                    .appendEncodedPath(String.valueOf(fromPosition))
                    .appendQueryParameter("move", "true")
                    .build();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, toPosition);
            return contentResolver.update(uri, values, null, null) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static int createPlaylist(String playlistName, Context context) {
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri playlists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            Cursor cursor = resolver.query(playlists, new String[]{MediaStore.Audio.Playlists.NAME, MediaStore.Audio.Playlists._ID}, null, null, null);
            long playlistId = 0;
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    try {
                        String plname = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                        if (plname.equalsIgnoreCase(playlistName)) {
                            playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
                cursor.close();
                if (playlistId == 0) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Audio.Playlists.NAME, playlistName);
                    contentValues.put(MediaStore.Audio.Playlists.DATE_MODIFIED,
                            System.currentTimeMillis());
                    resolver.insert(playlists, contentValues);
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getPlaylistID(String playlistName, Context context) {
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri playlists = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            Cursor cursor = resolver.query(playlists, new String[]{MediaStore.Audio.Playlists.NAME, MediaStore.Audio.Playlists._ID}, null, null, null);
            long playlistId = 0;
            if (cursor != null) {
                cursor.moveToFirst();
                do {
                    try {
                        String plname = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));
                        if (plname.equalsIgnoreCase(playlistName)) {
                            playlistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
                cursor.close();
                return playlistId;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static ArrayList<Song> getSongsOfArtist(ContentResolver contentResolver, String sortOrder, String artistName) {
        ArrayList<Song> songs = new ArrayList<>();
        try {
            String selection = BASE_SELECTION;
            String[] selectionValues = new String[0];
            ArrayList<String> paths = BlacklistStore.getInstance(SymphonyApplication.getInstance().getApplicationContext()).getPaths();
            if (!paths.isEmpty()) {
                selection = generateBlacklistSelection(selection, paths.size());
                selectionValues = addBlacklistSelectionValues(selectionValues, paths);
            }
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = contentResolver.query(uri, null, selection, selectionValues, sortOrder, null);
            if (cursor != null && cursor.moveToFirst()) {
                int titleColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.TITLE);
                int idColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media._ID);
                int artistColumn = cursor.getColumnIndex
                        (MediaStore.Audio.Media.ARTIST);
                int artColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int dateColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                do {
                    long thisId = cursor.getLong(idColumn);
                    String thisTitle = cursor.getString(titleColumn);
                    String thisArtist = cursor.getString(artistColumn);
                    long thisArt = cursor.getLong(artColumn);
                    String thisData = cursor.getString(dataColumn);
                    String thisAlbum = cursor.getString(albumColumn);
                    int duration = cursor.getInt(durationColumn);
                    int thisDate = cursor.getInt(dateColumn);
                    long artistId = cursor.getInt(artistIdColumn);
                    int trackNumber = cursor.getInt(trackNumberColumn);
                    int year = cursor.getInt(yearColumn);
                    if (thisArtist.equals(artistName)) {
                        songs.add(new Song(thisId, thisTitle, thisArtist, thisArt, thisData, thisAlbum, thisDate, duration, artistId, trackNumber, year));
                    }
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return songs;
    }

    public static ArrayList<Album> getAlbumsOfArtist(ContentResolver contentResolver, String artistName) {
        ArrayList<Song> songs = getSongsOfArtist(contentResolver, MediaStore.Audio.Media.TITLE, artistName);
        ArrayList<Album> albums = splitIntoAlbums(songs);
        sortAlbumsByName(albums);
        return albums;
    }

    public static void deletePlaylist(Context context, long playlistID) {
        ContentResolver resolver = context.getContentResolver();
        String where = MediaStore.Audio.Playlists._ID + "=?";
        String[] whereVal = {Long.toString(playlistID)};
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
        postToast(R.string.playlist_deleted, context, TOAST_SUCCESS);
    }

    public static void addToPlaylist(@NonNull final ContentResolver contentResolver, final int audioID, final long playlistID) {
        String[] cols = new String[]{
                MediaStore.Audio.Playlists.Members.AUDIO_ID
        };
        String sortOrder = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID);
        Cursor cursor = contentResolver.query(uri, cols, null, null, sortOrder);
        if (cursor != null) {
            final int base = cursor.getCount();
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base);
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioID);
            contentResolver.insert(uri, values);
        }
    }

    public static void addListToPlaylist(@NonNull ContentResolver contentResolver, ArrayList<Song> songs, long playlistID) {
        for (Song song : songs) {
            addToPlaylist(contentResolver, (int) song.getId(), playlistID);
        }
    }

    public static void removeFromPlaylist(final ContentResolver contentResolver, final int audioID, final long playlistID) {
        AsyncTask.execute(() -> {
            String[] cols = new String[]{
                    MediaStore.Audio.Playlists.Members.AUDIO_ID
            };
            String sortOrder = MediaStore.Audio.Playlists.Members.PLAY_ORDER;
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID);
            Cursor cursor = contentResolver.query(uri, cols, null, null, sortOrder);
            if (cursor != null) {
                cursor.moveToFirst();
                cursor.close();
                contentResolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + " = " + audioID, null);
            }
        });
    }

    public static boolean isInPlaylist(int id, String path, Context context) {
        try {
            boolean isItTrue = false;
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                do {
                    String thisData = cursor.getString(dataColumn);
                    if (thisData.equals(path)) {
                        isItTrue = true;
                        break;
                    }
                }
                while (cursor.moveToNext());
                cursor.close();
            }
            return isItTrue;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String generateBlacklistSelection(String selection, int pathCount) {
        String newSelection = selection != null && !selection.trim().equals("") ? selection + " AND " : "";
        newSelection += MediaStore.Audio.AudioColumns.DATA + " NOT LIKE ?";
        StringBuilder newSelectionBuilder = new StringBuilder(newSelection);
        for (int i = 0; i < pathCount - 1; i++) {
            newSelectionBuilder.append(" AND " + MediaStore.Audio.AudioColumns.DATA + " NOT LIKE ?");
        }
        newSelection = newSelectionBuilder.toString();
        return newSelection;
    }

    private static String[] addBlacklistSelectionValues(String[] selectionValues, ArrayList<String> paths) {
        if (selectionValues == null) selectionValues = new String[0];
        String[] newSelectionValues = new String[selectionValues.length + paths.size()];
        System.arraycopy(selectionValues, 0, newSelectionValues, 0, selectionValues.length);
        for (int i = selectionValues.length; i < newSelectionValues.length; i++) {
            newSelectionValues[i] = paths.get(i - selectionValues.length) + "%";
        }
        return newSelectionValues;
    }

    public static ArrayList<Song> removeNonExistentSongs(ArrayList<Song> songs) {
        ArrayList<Song> newSongs = new ArrayList<>(songs);
        for (Song song : songs) {
            if (!(new File(song.getPath())).exists()) {
                newSongs.remove(song);
            }
        }
        return newSongs;
    }

    public static MostPlayedList removeNonExistentSongs(ArrayList<Song> songs, ArrayList<Integer> playCountList) {
        ArrayList<Song> newSongs = new ArrayList<>();
        ArrayList<Integer> newPlayCountList = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            if ((new File(songs.get(i).getPath())).exists()) {
                newSongs.add(songs.get(i));
                newPlayCountList.add(playCountList.get(i));
            }
        }
        return new MostPlayedList(newSongs, newPlayCountList);
    }
}