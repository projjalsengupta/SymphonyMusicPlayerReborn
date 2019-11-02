package music.symphony.com.materialmusicv2.utils.lyricsutils;

import android.content.Context;
import android.content.ContextWrapper;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import music.symphony.com.materialmusicv2.R;

public class LyricsUtils {
    public static String getLyrics(String path, Context context, long ID) {
        try {
            if (context == null) {
                return null;
            }
            String lyrics = getLyricsFromCache(context, ID);
            if (!lyrics.equals(context.getString(R.string.no_lyrics))) {
                return lyrics;
            } else {
                File audio = new File(path);
                AudioFile f;
                f = AudioFileIO.read(audio);
                if (f != null) {
                    Tag tag = f.getTag();
                    lyrics = tag.getFirst(FieldKey.LYRICS).trim();
                    if (lyrics.length() != 0) {
                        setLyricsInCache(context, lyrics, ID);
                        return lyrics;
                    }
                }
                return context.getString(R.string.no_lyrics);
            }
        } catch (Exception e) {
            return context.getString(R.string.no_lyrics);
        }
    }

    public static String getLyricsFromCache(Context context, long ID) {
        if (context == null) {
            return null;
        }
        BufferedReader reader = null;
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("SymphonyLyrics", Context.MODE_PRIVATE);
            File myFile = new File(directory, Long.toString(ID) + ".txt");
            if (myFile.exists()) {
                reader = new BufferedReader(new FileReader(myFile));
                StringBuilder textBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    textBuilder.append(line);
                    textBuilder.append("\n");
                }
                return textBuilder.toString();
            } else {
                return context.getString(R.string.no_lyrics);
            }
        } catch (Exception e) {
            return context.getString(R.string.no_lyrics);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setLyricsInCache(Context context, String lyrics, long ID) {
        try {
            if (context == null || lyrics == null) {
                return;
            }
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("SymphonyLyrics", Context.MODE_PRIVATE);
            File myFile = new File(directory, Long.toString(ID) + ".txt");
            if (myFile.createNewFile()) {
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(lyrics);
                myOutWriter.close();
                fOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteLyricsFromCache(Context context, long ID) {
        try {
            if (context == null) {
                return false;
            }
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("SymphonyLyrics", Context.MODE_PRIVATE);
            File myFile = new File(directory, Long.toString(ID) + ".txt");
            return myFile.exists() && myFile.delete();
        } catch (Exception e) {
            return false;
        }
    }
}
