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

public abstract class JsonEncodable implements JsonSerializable {

    @NonNull
    static <E extends JsonEncodable> String encodeFrom(@NonNull E encodable) throws JSONException {
        return encodeFrom(encodable, Base64Coder.DEFAULT_FLAGS);
    }

    @NonNull
    static <E extends JsonEncodable> String encodeFrom(@NonNull E encodable, int flags) throws JSONException {
        return encodable.encode(flags);
    }

    @NonNull
    String encode(int flags) throws JSONException {
        return Base64Coder.encodeToString(serialize(), flags);
    }
}
