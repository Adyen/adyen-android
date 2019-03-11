/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 04/07/2018.
 */

package com.adyen.checkout.base.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public abstract class JsonObject implements Parcelable {
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("UTC");

    private final JSONObject mJsonObject;

    @NonNull
    public static <T extends JsonObject> T parseFrom(@NonNull JSONObject jsonObject, @NonNull Class<T> clazz) throws JSONException {
        try {
            return Objects.reflectiveInit(clazz, jsonObject);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof JSONException) {
                throw ((JSONException) cause);
            } else {
                throw new RuntimeException("Could not instantiate " + clazz.getSimpleName() + ".", cause);
            }
        }
    }

    @NonNull
    public static <T extends Enum<T>> T parseEnumValue(@NonNull String enumValue, @NonNull Class<T> clazz) throws JSONException {
        for (Field field : clazz.getFields()) {
            if (enumValue.equalsIgnoreCase(field.getName())) {
                //noinspection RedundantTypeArguments, type arguments need to be present for compiler
                return JsonObject.<T>getEnumValueFromField(field);
            }

            SerializedName serializedName = field.getAnnotation(SerializedName.class);

            if (serializedName != null && enumValue.equalsIgnoreCase(serializedName.value())) {
                //noinspection RedundantTypeArguments, type arguments need to be present for compiler
                return JsonObject.<T>getEnumValueFromField(field);
            }
        }

        throw new JSONException("Could not find enum constant for value " + enumValue);
    }

    @NonNull
    private static <T extends Enum<T>> T getEnumValueFromField(@NonNull Field field) throws JSONException {
        try {
            //noinspection unchecked
            return ((T) field.get(null));
        } catch (IllegalAccessException e) {
            throw new JSONException("Could not access enum constant " + field.getName() + ".");
        }
    }

    @NonNull
    public static JSONObject serialize(@NonNull JsonObject jsonObject) {
        return jsonObject.mJsonObject;
    }

    @Nullable
    public static JSONObject serializeOptional(@Nullable JsonObject jsonObject) {
        return jsonObject != null ? jsonObject.mJsonObject : null;
    }

    protected JsonObject(@NonNull JSONObject jsonObject) throws JSONException {
        mJsonObject = new JSONObject(jsonObject.toString());
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(@NonNull Parcel dest, int flags) {
        Parcelables.writeJsonObject(dest, mJsonObject);
    }

    @NonNull
    protected JSONObject getJsonObject() {
        return mJsonObject;
    }

    @NonNull
    protected final <T extends JsonObject> T parse(@NonNull String key, @NonNull Class<T> clazz) throws JSONException {
        JSONObject jsonObject = mJsonObject.getJSONObject(key);

        return parseFrom(jsonObject, clazz);
    }

    @Nullable
    protected final <T extends JsonObject> T parseOptional(@NonNull String key, @NonNull Class<T> clazz) throws JSONException {
        if (mJsonObject.has(key)) {
            return parse(key, clazz);
        } else {
            return null;
        }
    }

    @SafeVarargs
    @NonNull
    protected final <T extends JsonObject> List<T> parseList(@NonNull String key, @NonNull Class<T>... classes) throws JSONException {
        List<T> result = new ArrayList<>();

        JSONArray jsonArray = mJsonObject.getJSONArray(key);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Class<? extends T> clazz = classes[i % classes.length];
            T item = parseFrom(jsonObject, clazz);
            result.add(item);
        }

        return result;
    }

    @SafeVarargs
    @Nullable
    protected final <T extends JsonObject> List<T> parseOptionalList(@NonNull String key, @NonNull Class<T>... classes) throws JSONException {
        if (mJsonObject.has(key)) {
            return parseList(key, classes);
        } else {
            return null;
        }
    }

    @NonNull
    protected final <T extends Enum<T>> T parseEnum(@NonNull String key, @NonNull Class<T> clazz) throws JSONException {
        String enumValue = mJsonObject.getString(key);

        return parseEnumValue(enumValue, clazz);
    }

    @NonNull
    protected final Date parseDate(@NonNull String key) throws JSONException {
        String dateString = mJsonObject.getString(key);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            dateFormat.setTimeZone(TIME_ZONE);

            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new JSONException("Invalid date string: " + dateString);
        }
    }

    @Nullable
    protected final Date parseOptionalDate(@NonNull String key) throws JSONException {
        if (mJsonObject.has(key)) {
            return parseDate(key);
        } else {
            return null;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SerializedName {
        @NonNull
        String value();
    }

    protected abstract static class Creator<T extends JsonObject> implements Parcelable.Creator<T> {
        @Override
        public final T createFromParcel(@NonNull Parcel source) {
            JSONObject jsonObject = Parcelables.readJsonObject(source);
            Objects.requireNonNull(jsonObject, "JSONObject is null.");

            try {
                return createFromJson(jsonObject);
            } catch (JSONException e) {
                throw new RuntimeException("Invalid JSON.", e);
            }
        }

        @NonNull
        public abstract T createFromJson(@NonNull JSONObject jsonObject) throws JSONException;
    }

    protected static final class DefaultCreator<T extends JsonObject> extends Creator<T> {
        private final Class<T> mClass;

        public DefaultCreator(@NonNull Class<T> clazz) {
            mClass = clazz;
        }

        @NonNull
        @Override
        public T createFromJson(@NonNull JSONObject jsonObject) throws JSONException {
            return parseFrom(jsonObject, mClass);
        }

        @Override
        public T[] newArray(int size) {
            //noinspection unchecked
            return (T[]) Array.newInstance(mClass, size);
        }
    }
}
