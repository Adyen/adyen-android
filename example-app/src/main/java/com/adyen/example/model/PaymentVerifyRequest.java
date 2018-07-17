package com.adyen.example.model;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/08/2017.
 */
public final class PaymentVerifyRequest {
    @Json(name = "payload")
    private String mPayload;

    public PaymentVerifyRequest(@NonNull String payload) {
        mPayload = payload;
    }
}
