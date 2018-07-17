package com.adyen.checkout.core.internal.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonSerializable;
import com.adyen.checkout.base.internal.Parcelables;
import com.adyen.checkout.core.model.PaymentMethodDetails;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/08/2017.
 */
public final class PaymentInitiation implements Parcelable, JsonSerializable {
    public static final Creator<PaymentInitiation> CREATOR = new Creator<PaymentInitiation>() {
        @Override
        public PaymentInitiation createFromParcel(Parcel parcel) {
            return new PaymentInitiation(parcel);
        }

        @Override
        public PaymentInitiation[] newArray(int size) {
            return new PaymentInitiation[size];
        }
    };

    private String mPaymentData;

    private String mPaymentMethodData;

    private PaymentMethodDetails mPaymentMethodDetails;

    private PaymentInitiation() {
        // Empty constructor for Builder.
    }

    private PaymentInitiation(@NonNull Parcel in) {
        mPaymentData = in.readString();
        mPaymentMethodData = in.readString();
        mPaymentMethodDetails = Parcelables.read(in, PaymentMethodDetails.class);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mPaymentData);
        parcel.writeString(mPaymentMethodData);
        Parcelables.write(parcel, mPaymentMethodDetails);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("paymentData", mPaymentData);
        jsonObject.put("paymentMethodData", mPaymentMethodData);
        jsonObject.putOpt("paymentDetails", mPaymentMethodDetails != null ? mPaymentMethodDetails.serialize() : null);

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

        PaymentInitiation that = (PaymentInitiation) o;

        if (mPaymentData != null ? !mPaymentData.equals(that.mPaymentData) : that.mPaymentData != null) {
            return false;
        }
        if (mPaymentMethodData != null ? !mPaymentMethodData.equals(that.mPaymentMethodData) : that.mPaymentMethodData != null) {
            return false;
        }
        return mPaymentMethodDetails != null ? mPaymentMethodDetails.equals(that.mPaymentMethodDetails) : that.mPaymentMethodDetails == null;
    }

    @Override
    public int hashCode() {
        int result = mPaymentData != null ? mPaymentData.hashCode() : 0;
        result = 31 * result + (mPaymentMethodData != null ? mPaymentMethodData.hashCode() : 0);
        result = 31 * result + (mPaymentMethodDetails != null ? mPaymentMethodDetails.hashCode() : 0);
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
        private PaymentInitiation mPaymentInitiation;

        public Builder(@NonNull String paymentData, @NonNull String paymentMethodData) {
            mPaymentInitiation = new PaymentInitiation();
            mPaymentInitiation.mPaymentData = paymentData;
            mPaymentInitiation.mPaymentMethodData = paymentMethodData;
        }

        @NonNull
        public Builder setPaymentMethodDetails(@Nullable PaymentMethodDetails paymentMethodDetails) {
            mPaymentInitiation.mPaymentMethodDetails = paymentMethodDetails;

            return this;
        }

        @NonNull
        public PaymentInitiation build() {
            return mPaymentInitiation;
        }
    }
}
