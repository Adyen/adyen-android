package com.adyen.core.utils;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;


public final  class Util {
    /**
     * Converts a Map<String, Object> to JsonObject.
     * Only works for simple types in the map. (String, int, long)
     * @param map The map to be converted to a JsonObject.
     * @return A JsonObject. Null if map was null.
     */
    public static JSONObject mapToJson(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            final Set<Map.Entry<String, Object>> entries = map.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Utility class for converting InputStream to byte array.
     *
     * @param inputStream to be read
     * @return byte array
     * @throws IOException if InputStream cannot be read.
     */
    public static byte[] convertInputStreamToByteArray(@NonNull final InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] byteArrayBuffer = new byte[512];
        int nRead;
        while ((nRead = inputStream.read(byteArrayBuffer)) != -1) {
            output.write(byteArrayBuffer, 0, nRead);
        }
        return output.toByteArray();
    }

    private Util() {
        // Private constructor
    }
}
