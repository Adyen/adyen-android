/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 18/10/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link PaymentMethodDetails} for Android Pay payments. This class contains only tokenized and encrypted card data.
 */
public final class AndroidPayDetails extends PaymentMethodDetails {
    @NonNull
    public static final Creator<AndroidPayDetails> CREATOR = new Creator<AndroidPayDetails>() {
        @Override
        public AndroidPayDetails createFromParcel(Parcel source) {
            return new AndroidPayDetails(source);
        }

        @Override
        public AndroidPayDetails[] newArray(int size) {
            return new AndroidPayDetails[size];
        }
    };

    @NonNull
    public static final String KEY_ADDITIONAL_DATA_TOKEN = "additionalData.androidpay.token";

    private String mToken;

    private AndroidPayDetails() {
        // Empty constructor for Builder.
    }

    private AndroidPayDetails(@NonNull Parcel in) {
        super(in);

        mToken = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mToken);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_ADDITIONAL_DATA_TOKEN, mToken);

        return jsonObject;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AndroidPayDetails that = (AndroidPayDetails) o;

        return mToken != null ? mToken.equals(that.mToken) : that.mToken == null;
    }

    @Override
    public int hashCode() {
        return mToken != null ? mToken.hashCode() : 0;
    }

    public static final class Builder {
        private final AndroidPayDetails mAndroidPayDetails;

        public Builder(@NonNull String token) {
            mAndroidPayDetails = new AndroidPayDetails();
            mAndroidPayDetails.mToken = token;
        }

        @NonNull
        public AndroidPayDetails build() {
            return mAndroidPayDetails;
        }
    }
}
