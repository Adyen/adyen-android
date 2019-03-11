/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 21/11/2018.
 */

package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JsonDecodable extends JsonObject {

    @NonNull
    public static <T extends JsonDecodable> T decodeFrom(@NonNull String encodedData, @NonNull Class<T> clazz) throws JSONException {
        return decodeFrom(encodedData, clazz, Base64Coder.DEFAULT_FLAGS);
    }

    @NonNull
    public static <T extends JsonDecodable> T decodeFrom(@NonNull String encodedData, @NonNull Class<T> clazz, int flags) throws JSONException {
        JSONObject jsonObject = Base64Coder.decodeToJSONObject(encodedData, flags);

        return parseFrom(jsonObject, clazz);
    }

    protected JsonDecodable(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }
}
