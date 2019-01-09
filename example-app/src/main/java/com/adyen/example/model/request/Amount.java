/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/11/2018.
 */

package com.adyen.example.model.request;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

import java.io.Serializable;

public final class Amount implements Serializable {
    @Json(name = "value")
    private Long mValue;

    @Json(name = "currency")
    private String mCurrency;

    public Amount(@NonNull Long value, @NonNull String currency) {
        mValue = value;
        mCurrency = currency;
    }

    @NonNull
    public Long getValue() {
        return mValue;
    }

    @NonNull
    public String getCurrency() {
        return mCurrency;
    }
}
