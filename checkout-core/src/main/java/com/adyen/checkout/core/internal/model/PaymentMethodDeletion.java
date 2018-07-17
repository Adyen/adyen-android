package com.adyen.checkout.core.internal.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonSerializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/08/2017.
 */
public final class PaymentMethodDeletion implements Parcelable, JsonSerializable {
    public static final Creator<PaymentMethodDeletion> CREATOR = new Creator<PaymentMethodDeletion>() {
        @Override
        public PaymentMethodDeletion createFromParcel(Parcel parcel) {
            return new PaymentMethodDeletion(parcel);
        }

        @Override
        public PaymentMethodDeletion[] newArray(int size) {
            return new PaymentMethodDeletion[size];
        }
    };

    private String mPaymentData;

    private String mPaymentMethodData;

    private PaymentMethodDeletion() {
        // Empty constructor for Builder.
    }

    private PaymentMethodDeletion(@NonNull Parcel in) {
        mPaymentData = in.readString();
        mPaymentMethodData = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mPaymentData);
        parcel.writeString(mPaymentMethodData);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("paymentData", mPaymentData);
        jsonObject.put("paymentMethodData", mPaymentMethodData);

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

        PaymentMethodDeletion that = (PaymentMethodDeletion) o;

        if (mPaymentData != null ? !mPaymentData.equals(that.mPaymentData) : that.mPaymentData != null) {
            return false;
        }
        return mPaymentMethodData != null ? mPaymentMethodData.equals(that.mPaymentMethodData) : that.mPaymentMethodData == null;
    }

    @Override
    public int hashCode() {
        int result = mPaymentData != null ? mPaymentData.hashCode() : 0;
        result = 31 * result + (mPaymentMethodData != null ? mPaymentMethodData.hashCode() : 0);
        return result;
    }

    @NonNull
    public String getPaymentData() {
        return mPaymentData;
    }

    @NonNull
    public String getPaymentMethodData() {
        return mPaymentMethodData;
    }

    public static final class Builder {
        private PaymentMethodDeletion mPaymentMethodDeletion;

        public Builder(@NonNull String paymentData, @NonNull String paymentMethodData) {
            mPaymentMethodDeletion = new PaymentMethodDeletion();
            mPaymentMethodDeletion.mPaymentData = paymentData;
            mPaymentMethodDeletion.mPaymentMethodData = paymentMethodData;
        }

        @NonNull
        public PaymentMethodDeletion build() {
            return mPaymentMethodDeletion;
        }
    }
}
