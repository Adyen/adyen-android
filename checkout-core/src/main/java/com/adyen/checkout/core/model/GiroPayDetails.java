/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 22/11/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public final class GiroPayDetails extends PaymentMethodDetails {
    @NonNull
    public static final Creator<GiroPayDetails> CREATOR = new Creator<GiroPayDetails>() {
        @Override
        public GiroPayDetails createFromParcel(Parcel source) {
            return new GiroPayDetails(source);
        }

        @Override
        public GiroPayDetails[] newArray(int size) {
            return new GiroPayDetails[size];
        }
    };

    @NonNull
    public static final String KEY_GIROPAY_BIC = "bic";

    private String mBic;

    private GiroPayDetails() {
        // Empty constructor for Builder.
    }

    private GiroPayDetails(@NonNull Parcel in) {
        super(in);

        mBic = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mBic);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_GIROPAY_BIC, mBic);

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

        GiroPayDetails that = (GiroPayDetails) o;

        return mBic != null ? mBic.equals(that.mBic) : that.mBic == null;
    }

    @Override
    public int hashCode() {
        return mBic != null ? mBic.hashCode() : 0;
    }

    public static final class Builder {
        private GiroPayDetails mGiroPayDetails;

        @NonNull
        public Builder setBic(@Nullable String bic) {
            if (bic != null) {
                if (mGiroPayDetails == null) {
                    mGiroPayDetails = new GiroPayDetails();
                }

                mGiroPayDetails.mBic = bic;
            } else {
                mGiroPayDetails = null;
            }

            return this;
        }

        @Nullable
        public GiroPayDetails build() {
            return mGiroPayDetails;
        }
    }
}
