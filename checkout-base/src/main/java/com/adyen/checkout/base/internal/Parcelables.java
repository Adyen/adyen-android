/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 05/08/2017.
 */

package com.adyen.checkout.base.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Parcelables {
    private static final int FLAG_NULL = 0;

    private static final int FLAG_NON_NULL = FLAG_NULL + 1;

    private Parcelables() {
        throw new IllegalStateException("No instances.");
    }

    public static <T extends Parcelable> void write(@NonNull Parcel parcel, @Nullable T value) {
        if (value == null) {
            parcel.writeInt(FLAG_NULL);
        } else {
            parcel.writeInt(FLAG_NON_NULL);
            parcel.writeParcelable(value, 0);
        }
    }

    public static <T extends Serializable> void writeSerializable(@NonNull Parcel parcel, @Nullable T value) {
        if (value == null) {
            parcel.writeInt(FLAG_NULL);
        } else {
            parcel.writeInt(FLAG_NON_NULL);
            parcel.writeSerializable(value);
        }
    }

    public static <T extends Parcelable> void writeList(@NonNull Parcel parcel, @Nullable List<T> list) {
        if (list == null) {
            parcel.writeInt(FLAG_NULL);
        } else {
            parcel.writeInt(FLAG_NON_NULL);
            parcel.writeList(list);
        }
    }

    public static void writeJsonObject(@NonNull Parcel parcel, @Nullable JSONObject jsonObject) {
        if (jsonObject == null) {
            parcel.writeInt(FLAG_NULL);
        } else {
            parcel.writeInt(FLAG_NON_NULL);
            parcel.writeString(jsonObject.toString());
        }
    }

    @Nullable
    public static <T extends Parcelable> T read(@NonNull Parcel parcel, @NonNull Class<T> clazz) {
        int val = parcel.readInt();

        switch (val) {
            case FLAG_NULL:
                return null;
            case FLAG_NON_NULL:
                return parcel.readParcelable(clazz.getClassLoader());
            default:
                throw new IllegalArgumentException("Invalid flag.");
        }
    }

    @Nullable
    public static <T extends Serializable> T readSerializable(@NonNull Parcel parcel) {
        switch (parcel.readInt()) {
            case FLAG_NULL:
                return null;
            case FLAG_NON_NULL:
                // noinspection unchecked
                return (T) parcel.readSerializable();
            default:
                throw new IllegalArgumentException("Invalid flag.");
        }
    }

    @Nullable
    public static <T extends Parcelable> List<T> readList(@NonNull Parcel parcel, @NonNull Class clazz) {
        switch (parcel.readInt()) {
            case FLAG_NULL:
                return null;
            case FLAG_NON_NULL:
                List<T> result = new ArrayList<>();
                parcel.readList(result, clazz.getClassLoader());
                return result;
            default:
                throw new IllegalArgumentException("Invalid flag.");
        }
    }

    @Nullable
    public static JSONObject readJsonObject(@NonNull Parcel parcel) {
        switch (parcel.readInt()) {
            case FLAG_NULL:
                return null;
            case FLAG_NON_NULL:
                try {
                    return new JSONObject(parcel.readString());
                } catch (JSONException e) {
                    throw new RuntimeException("Invalid JSON.", e);
                }
            default:
                throw new IllegalArgumentException("Invalid flag.");
        }
    }
}
