package music.symphony.com.materialmusicv2.glide.artist;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.net.URLEncoder;
import java.util.Iterator;

import music.symphony.com.materialmusicv2.R;
import music.symphony.com.materialmusicv2.SymphonyApplication;
import music.symphony.com.materialmusicv2.methodcalls.MethodCalls;
import music.symphony.com.materialmusicv2.utils.bitmaputils.BitmapUtils;

class LoaderUtils {
    static byte[] getArtistImageByteArrayFromName(String artistName) {
        try {
            if (artistName == null || artistName.equals("")) {
                return null;
            }

            Context context = SymphonyApplication.getInstance().getApplicationContext();
            String url = String.format(context.getString(R.string.artist_get_info), URLEncoder.encode(artistName, "UTF-8"), context.getString(R.string.last_fm_api_key));

            String response = MethodCalls.get(url);

            if (response != null) {
                String id = getArtistMBIDFrom(response);

                if (id != null) {
                    url = String.format(context.getString(R.string.artist_get_image_from_fanart), id, context.getString(R.string.fanart_api_key));

                    response = MethodCalls.get(url);

                    if (response != null) {
                        url = getArtistImageURL(response);
                        if (url != null && !url.equals("")) {
                            return BitmapUtils.decodeUrl(url);
                        }
                    }

                    url = String.format(context.getString(R.string.artist_get_information), URLEncoder.encode(id, "UTF-8"));

                    response = MethodCalls.get(url);

                    if (response != null) {
                        url = getArtistImageURLFromXML(context, response);
                        if (url != null) {
                            return BitmapUtils.decodeUrl(url);
                        }
                    }
                }
            }

            return BitmapUtils.decodeUrl("https://tse2.mm.bing.net/th?q=" + URLEncoder.encode(artistName, "UTF-8") + "%20artist+spotify.com&w=300&h=300&c=7&rs=1&p=0&dpr=3&pid=1.7&mkt=en-IN&adlt=on%27");
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getArtistImageURLFromXML(Context context, String response) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(response));
            int eventType = xpp.getEventType();

            String url = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("relation") && xpp.getAttributeValue(null, "type").equals("image")) {
                    xpp.next();
                    url = xpp.nextText();
                    break;
                }
                eventType = xpp.next();
            }

            if (url != null) {
                url = String.format(context.getString(R.string.get_image_url), url.substring(url.indexOf("File:")));

                response = MethodCalls.get(url);

                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        jsonObject = jsonObject.getJSONObject("query");
                        jsonObject = jsonObject.getJSONObject("pages");
                        Iterator<String> keys = jsonObject.keys();
                        String id = String.valueOf(keys.next());
                        JSONObject object = jsonObject.getJSONObject(id);
                        JSONArray jsonArray = object.getJSONArray("imageinfo");
                        JSONObject imageObject = (JSONObject) jsonArray.get(0);
                        return imageObject.getString("thumburl");
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getArtistMBIDFrom(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonObject = jsonObject.getJSONObject("artist");
            return jsonObject.getString("mbid");
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String getArtistImageURL(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("artistthumb");
            return ((JSONObject) jsonArray.get(0)).getString("url");
        } catch (Exception ignored) {
            return null;
        }
    }
}
