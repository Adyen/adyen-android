package com.adyen.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

final class IconStorage {

    private static final String PREFS_NAME = "checkout_icons";

    private IconStorage() {

    }

    static Bitmap getIcon(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String strPrefs = prefs.getString(url, null);
        IconData iconData = null;
        if (strPrefs != null) {
            try {
                iconData = new IconData(strPrefs);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        if (iconData != null && !iconData.needsRefreshing()) {
            return iconData.getBitmap();
        } else {
            return null;
        }
    }

    static void storeIcon(Context context, Bitmap bitmap, String url) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();

        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

        IconData iconData = new IconData(System.currentTimeMillis(), encodedImage);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();

        try {
            edit.putString(url, iconData.serialize());
            edit.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final class IconData {
        private long lastModified;
        private String encodedImage;

        private static final int DAYS_UNTIL_LOGOS_WILL_REFRESH = 30;

        private static final String KEY_LAST_MODIFIED = "last_modified";
        private static final String KEY_ENCODED_IMAGE = "encoded_image";

        private IconData(String jsonString) throws JSONException {
            JSONObject jsonObject = new JSONObject(jsonString);
            this.lastModified = jsonObject.getLong(KEY_LAST_MODIFIED);
            this.encodedImage = jsonObject.getString(KEY_ENCODED_IMAGE);
        }

        private IconData(long lastModified, String encodedImage) {
            this.lastModified = lastModified;
            this.encodedImage = encodedImage;
        }

        private String serialize() throws JSONException {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(KEY_ENCODED_IMAGE, encodedImage);
            jsonObject.put(KEY_LAST_MODIFIED, lastModified);
            return jsonObject.toString();
        }

        Bitmap getBitmap() {
            byte[] b = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }

        private boolean needsRefreshing() {
            final long timeFrame = TimeUnit.MILLISECONDS.convert(DAYS_UNTIL_LOGOS_WILL_REFRESH, TimeUnit.DAYS);
            return System.currentTimeMillis() - lastModified > timeFrame;
        }
    }
}
