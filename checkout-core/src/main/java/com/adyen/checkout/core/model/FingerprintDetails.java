/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 19/11/2018.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public final class FingerprintDetails extends PaymentMethodDetails {
    public static final Creator<FingerprintDetails> CREATOR = new Creator<FingerprintDetails>() {
        @Override
        public FingerprintDetails createFromParcel(Parcel source) {
            return new FingerprintDetails(source);
        }

        @Override
        public FingerprintDetails[] newArray(int size) {
            return new FingerprintDetails[size];
        }
    };

    public static final String KEY_THREE_DS_FINGERPRINT = "threeds2.fingerprint";

    private final String mFingerprint;

    public FingerprintDetails(@NonNull String fingerprint) {
        mFingerprint = fingerprint;
    }

    protected FingerprintDetails(@NonNull Parcel in) {
        super(in);

        this.mFingerprint = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(this.mFingerprint);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_THREE_DS_FINGERPRINT, mFingerprint);

        return jsonObject;
    }
}
