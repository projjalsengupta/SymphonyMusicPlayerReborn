package music.symphony.com.materialmusicv2.utils.artistutils;

import android.content.Context;
import android.content.ContextWrapper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.methodcalls.MethodCalls;

public class ArtistUtils {
    public static String getArtistBioFromNameAndID(String artistName, long id) {
        try {
            if (artistName == null || artistName.equals("")) {
                return null;
            }

            Context context = SymphonyApplication.getInstance().getApplicationContext();

            String biography = ArtistUtils.getBioFromCache(context, id);

            if (biography != null) {
                return biography;
            }

            String url = String.format(context.getString(R.string.artist_get_info), URLEncoder.encode(artistName, "UTF-8"), context.getString(R.string.last_fm_api_key));

            String response = MethodCalls.get(url);

            biography = null;

            if (response != null) {
                url = null;
                JSONObject artist = (new JSONObject(response).getJSONObject("artist"));
                JSONObject bio = artist.getJSONObject("bio");
                biography = bio.getString("content");
                ArtistUtils.setBioInCache(context, biography, id);
            }
            return biography;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getBioFromCache(Context context, long ID) {
        if (context == null) {
            return null;
        }
        BufferedReader reader = null;
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("SymphonyBio", Context.MODE_PRIVATE);
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
                return null;
            }
        } catch (Exception e) {
            return null;
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

    private static void setBioInCache(Context context, String bio, long ID) {
        try {
            if (context == null || bio == null) {
                return;
            }
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("SymphonyBio", Context.MODE_PRIVATE);
            File myFile = new File(directory, Long.toString(ID) + ".txt");
            if (myFile.createNewFile()) {
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(bio);
                myOutWriter.close();
                fOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
