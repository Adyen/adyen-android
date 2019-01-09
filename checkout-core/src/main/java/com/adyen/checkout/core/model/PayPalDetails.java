/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/08/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.Parcelables;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link PaymentMethodDetails} for PayPal payments.
 */
public final class PayPalDetails extends PaymentMethodDetails {
    @NonNull
    public static final Creator<PayPalDetails> CREATOR = new Creator<PayPalDetails>() {
        @Override
        public PayPalDetails createFromParcel(Parcel parcel) {
            return new PayPalDetails(parcel);
        }

        @Override
        public PayPalDetails[] newArray(int size) {
            return new PayPalDetails[size];
        }
    };

    @NonNull
    public static final String KEY_STORE_DETAILS = "storeDetails";

    private Boolean mStoreDetails;

    private PayPalDetails() {
        // Empty constructor for Builder.
    }

    private PayPalDetails(@NonNull Parcel in) {
        super(in);

        mStoreDetails = Parcelables.readSerializable(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        Parcelables.writeSerializable(parcel, mStoreDetails);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_STORE_DETAILS, mStoreDetails);

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

        PayPalDetails that = (PayPalDetails) o;

        return mStoreDetails != null ? mStoreDetails.equals(that.mStoreDetails) : that.mStoreDetails == null;
    }

    @Override
    public int hashCode() {
        return mStoreDetails != null ? mStoreDetails.hashCode() : 0;
    }

    public static final class Builder {
        private PayPalDetails mPayPalDetails;

        @NonNull
        public Builder setStoreDetails(@Nullable Boolean storeDetails) {
            if (storeDetails != null) {
                if (mPayPalDetails == null) {
                    mPayPalDetails = new PayPalDetails();
                }

                mPayPalDetails.mStoreDetails = storeDetails;
            } else {
                mPayPalDetails = null;
            }

            return this;
        }

        @Nullable
        public PayPalDetails build() {
            return mPayPalDetails;
        }
    }
}
