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

public final class PaymentVerifyResponse {
    @Json(name = "authResponse")
    private ResultCode mAuthResponse;

    @Json(name = "resultCode")
    private ResultCode mResultCode;

    @Json(name = "merchantReference")
    private String mMerchantReference;

    @Json(name = "pspReference")
    private String mPspReference;

    @NonNull
    public ResultCode getResultCode() {
        return mResultCode != null ? mResultCode : mAuthResponse;
    }

    @NonNull
    public String getMerchantReference() {
        return mMerchantReference;
    }

    @NonNull
    public String getPspReference() {
        return mPspReference;
    }

    /**
     * The authorization response.
     */
    public enum ResultCode {
        @Json(name = "Pending") PENDING,
        @Json(name = "Received") RECEIVED,
        @Json(name = "Authorised") AUTHORIZED,
        @Json(name = "Error") ERROR,
        @Json(name = "Refused") REFUSED,
        @Json(name = "Cancelled") CANCELLED,
        @Json(name = "Unknown") UNKNOWN
    }
}
