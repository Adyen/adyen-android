/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/4/2019.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.core.exception.NoConstructorException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class JsonUtils {

    public static final int INDENTATION_SPACES = 4;
    private static final String PARSING_ERROR = "PARSING_ERROR";
    private static final String NULL_OBJECT = "NULL";

    private static final int FLAG_NULL = 0;
    private static final int FLAG_NON_NULL = FLAG_NULL + 1;

    /**
     * Transforms a JSONObject to an indented string form.
     *
     * @param jsonObject The object, can be null.
     * @return The indented string version of the json.
     */
    @NonNull
    public static String indent(@Nullable JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                return jsonObject.toString(INDENTATION_SPACES);
            } catch (JSONException e) {
                return PARSING_ERROR;
            }
        } else {
            return NULL_OBJECT;
        }
    }

    /**
     * Writes a {@link JSONObject} to a {@link Parcel} as a {@link String}.
     *
     * @param parcel The Parcel to be written to.
     * @param jsonObject The JSONObject to be saved in the Parcel.
     */
    public static void writeToParcel(@NonNull Parcel parcel, @Nullable JSONObject jsonObject) {
        if (jsonObject == null) {
            parcel.writeInt(FLAG_NULL);
        } else {
            parcel.writeInt(FLAG_NON_NULL);
            parcel.writeString(jsonObject.toString());
        }
    }

    /**
     * Reads a {@link JSONObject} previously saved on a {@link Parcel}.
     *
     * @param parcel The Parcel to be read.
     * @return The JSONObject that was contained in the Parcel.
     */
    @Nullable
    public static JSONObject readFromParcel(@NonNull Parcel parcel) throws JSONException {
        switch (parcel.readInt()) {
            case FLAG_NULL:
                return null;
            case FLAG_NON_NULL:
                return new JSONObject(parcel.readString());
            default:
                throw new IllegalArgumentException("Invalid flag.");
        }
    }

    /**
     * Parses a {@link JSONArray} to a list of Strings.
     *
     * @param jsonArray The JSONArray to be read.
     * @return A {@link List} of strings, or null if the jsonArray was null.
     */
    @Nullable
    public static List<String> parseOptStringList(@Nullable JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }

        final List<String> list = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            final String item = jsonArray.optString(i, null);
            if (item != null) {
                list.add(item);
            }
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Serializes a List of String to a {@link JSONArray}.
     *
     * @param stringList The {@link List} of Strings to be serialized.
     * @return The populated {@link JSONArray}. Could be null.
     */
    @Nullable
    public static JSONArray serializeOptStringList(@Nullable List<String> stringList) {
        if (stringList == null) {
            return null;
        }

        final JSONArray jsonArray = new JSONArray();

        for (String string : stringList) {
            if (!TextUtils.isEmpty(string)) {
                jsonArray.put(string);
            }
        }

        return jsonArray;
    }

    private JsonUtils() {
        throw new NoConstructorException();
    }
}
