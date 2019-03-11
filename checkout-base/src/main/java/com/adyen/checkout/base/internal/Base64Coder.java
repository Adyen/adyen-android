/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 22/11/2018.
 */

package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

public final class Base64Coder {

    private static final Charset DEFAULT_CHARSET = Api.CHARSET;

    public static final int DEFAULT_FLAGS = Base64.DEFAULT;

    @NonNull
    public static <D extends JsonDecodable> D decode(@NonNull String encodedData, @NonNull Class<D> decodableClass) throws JSONException {
        return decode(encodedData, decodableClass, DEFAULT_FLAGS);
    }

    @NonNull
    public static <D extends JsonDecodable> D decode(@NonNull String encodedData, @NonNull Class<D> decodableClass, int flags) throws JSONException {
        D decodeable = JsonDecodable.decodeFrom(encodedData, decodableClass, flags);

        return decodeable;
    }

    @NonNull
    public static <E extends JsonEncodable> String encode(@NonNull E encodable) throws JSONException {
        return encode(encodable, DEFAULT_FLAGS);
    }

    @NonNull
    public static <E extends JsonEncodable> String encode(@NonNull E encodable, int flags) throws JSONException {
        return JsonEncodable.encodeFrom(encodable, flags);
    }

    @NonNull
    public static String encodeToString(@NonNull JSONObject jsonObject) {
        return encodeToString(jsonObject, DEFAULT_FLAGS);
    }

    @NonNull
    public static String encodeToString(@NonNull JSONObject jsonObject, int flags) {
        return encodeToString(jsonObject.toString(), flags);
    }

    @NonNull
    public static String encodeToString(@NonNull String decodedData) {
        return encodeToString(decodedData, DEFAULT_FLAGS);
    }

    @NonNull
    public static String encodeToString(@NonNull String decodedData, int flags) {
        byte[] decodedBytes = decodedData.getBytes(DEFAULT_CHARSET);

        return Base64.encodeToString(decodedBytes, flags);
    }

    @NonNull
    public static JSONObject decodeToJSONObject(@NonNull String encodedData) throws JSONException {
        return decodeToJSONObject(encodedData, DEFAULT_FLAGS);
    }

    @NonNull
    public static JSONObject decodeToJSONObject(@NonNull String encodedData, int flags) throws JSONException {
        String decodedData = decodeToString(encodedData, flags);

        return new JSONObject(decodedData);
    }

    @NonNull
    public static String decodeToString(@NonNull String encodedData) {
        return decodeToString(encodedData, DEFAULT_FLAGS);
    }

    @NonNull
    public static String decodeToString(@NonNull String encodedData, int flags) {
        byte[] decodedBytes = Base64.decode(encodedData, flags);
        String decodedData = new String(decodedBytes, DEFAULT_CHARSET);

        return decodedData;
    }

    private Base64Coder() {
        throw new IllegalStateException("No instances.");
    }
}
