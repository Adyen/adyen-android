package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 24/04/2018.
 */
public final class GooglePayDetails extends PaymentMethodDetails {
    public static final Creator<GooglePayDetails> CREATOR = new Creator<GooglePayDetails>() {
        @Override
        public GooglePayDetails createFromParcel(Parcel source) {
            return new GooglePayDetails(source);
        }

        @Override
        public GooglePayDetails[] newArray(int size) {
            return new GooglePayDetails[size];
        }
    };

    public static final String KEY_ADDITIONAL_DATA_TOKEN = "additionalData.paywithgoogle.token";

    private String mToken;

    private GooglePayDetails() {
        // Empty constructor for Builder.
    }

    private GooglePayDetails(@NonNull Parcel in) {
        super(in);

        mToken = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GooglePayDetails that = (GooglePayDetails) o;

        return mToken != null ? mToken.equals(that.mToken) : that.mToken == null;
    }

    @Override
    public int hashCode() {
        return mToken != null ? mToken.hashCode() : 0;
    }

    public static final class Builder {
        private final GooglePayDetails mGooglePayDetails;

        public Builder(@NonNull String token) {
            mGooglePayDetails = new GooglePayDetails();
            mGooglePayDetails.mToken = token;
        }

        @NonNull
        public GooglePayDetails build() {
            return mGooglePayDetails;
        }
    }
}
