/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/08/2017.
 */

package com.adyen.example.model;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

public final class PaymentVerifyRequest {
    @Json(name = "payload")
    private String mPayload;

    public PaymentVerifyRequest(@NonNull String payload) {
        mPayload = payload;
    }
}
