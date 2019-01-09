/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by emmanuel on 04/12/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public final class IssuerDetails extends PaymentMethodDetails {
    @NonNull
    public static final Parcelable.Creator<IssuerDetails> CREATOR = new Parcelable.Creator<IssuerDetails>() {
        @Override
        public IssuerDetails createFromParcel(@NonNull Parcel parcel) {
            return new IssuerDetails(parcel);
        }

        @Override
        public IssuerDetails[] newArray(int size) {
            return new IssuerDetails[size];
        }
    };

    @NonNull
    public static final String KEY_ISSUER = "issuer";

    private String mIssuer;

    private IssuerDetails() {
        // Empty constructor for Builder.
    }

    private IssuerDetails(@NonNull Parcel in) {
        super(in);

        mIssuer = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(mIssuer);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_ISSUER, mIssuer);
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

        IssuerDetails that = (IssuerDetails) o;

        return mIssuer != null ? mIssuer.equals(that.mIssuer) : that.mIssuer == null;
    }

    @Override
    public int hashCode() {
        return mIssuer != null ? mIssuer.hashCode() : 0;
    }

    public static final class Builder {
        private final IssuerDetails mIssuerDetails;

        public Builder(@NonNull String issuerId) {
            mIssuerDetails = new IssuerDetails();
            mIssuerDetails.mIssuer = issuerId;
        }

        @NonNull
        public IssuerDetails build() {
            return mIssuerDetails;
        }
    }
}
