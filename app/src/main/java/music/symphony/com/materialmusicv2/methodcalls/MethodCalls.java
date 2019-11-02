package music.symphony.com.materialmusicv2.methodcalls;

import android.content.Context;
import android.content.ContextWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import music.symphony.com.materialmusicv2.SymphonyApplication;

public class MethodCalls {
    public static String get(String urlString) {
        String response = getResponseFromCache(SymphonyApplication.getInstance().getApplicationContext(), urlString);
        if (response != null) {
            return response;
        }
        if (urlString != null) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(500);

                int statusCode = connection.getResponseCode();
                InputStream inputStream;
                if (statusCode >= 200 && statusCode < 400) {
                    inputStream = connection.getInputStream();
                } else {
                    inputStream = connection.getErrorStream();
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                if (statusCode >= 200 && statusCode < 400) {
                    setResponseInCache(SymphonyApplication.getInstance().getApplicationContext(), stringBuilder.toString(), urlString);
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    private static String getResponseFromCache(Context context, String url) {
        if (context == null) {
            return null;
        }
        BufferedReader reader = null;
        try {
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("SymphonyResponse", Context.MODE_PRIVATE);
            File myFile = new File(directory, url.hashCode() + ".txt");
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

    private static void setResponseInCache(Context context, String response, String url) {
        try {
            if (context == null || response == null) {
                return;
            }
            ContextWrapper cw = new ContextWrapper(context);
            File directory = cw.getDir("SymphonyResponse", Context.MODE_PRIVATE);
            File myFile = new File(directory, url.hashCode() + ".txt");
            if (myFile.createNewFile()) {
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(response);
                myOutWriter.close();
                fOut.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}