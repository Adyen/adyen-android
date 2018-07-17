package com.adyen.checkout.core.internal.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.PaymentMethodDetails;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link PaymentMethodDetails} for payments where the shopper was redirected directly to another app.
 * <p>
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 27/09/2017.
 */
public final class AppResponseDetails extends PaymentMethodDetails {
    public static final Creator<AppResponseDetails> CREATOR = new Creator<AppResponseDetails>() {
        @Override
        public AppResponseDetails createFromParcel(Parcel parcel) {
            return new AppResponseDetails(parcel);
        }

        @Override
        public AppResponseDetails[] newArray(int size) {
            return new AppResponseDetails[size];
        }
    };

    public static final String KEY_RETURN_URL_QUERY_STRING = "returnUrlQueryString";

    private String mReturnUrlQueryString;

    private AppResponseDetails() {
        // Empty constructor for Builder.
    }

    private AppResponseDetails(@NonNull Parcel in) {
        super(in);

        mReturnUrlQueryString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mReturnUrlQueryString);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject result = new JSONObject();
        result.put(KEY_RETURN_URL_QUERY_STRING, mReturnUrlQueryString);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AppResponseDetails that = (AppResponseDetails) o;

        return mReturnUrlQueryString != null ? mReturnUrlQueryString.equals(that.mReturnUrlQueryString) : that.mReturnUrlQueryString == null;
    }

    @Override
    public int hashCode() {
        return mReturnUrlQueryString != null ? mReturnUrlQueryString.hashCode() : 0;
    }

    public static final class Builder {
        private final AppResponseDetails mAppResponseDetails;

        public Builder(@NonNull String returnUrlQueryString) {
            mAppResponseDetails = new AppResponseDetails();
            mAppResponseDetails.mReturnUrlQueryString = returnUrlQueryString;
        }

        @NonNull
        public AppResponseDetails build() {
            return mAppResponseDetails;
        }
    }
}
