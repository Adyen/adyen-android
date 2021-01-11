/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.components;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails;
import com.adyen.checkout.core.util.ParcelUtils;

/**
 * The current state of a PaymentComponent.
 */
public class PaymentComponentState<PaymentMethodDetailsT extends PaymentMethodDetails> implements Parcelable {

    @NonNull
    public static final Parcelable.Creator<PaymentComponentState> CREATOR = new Parcelable.Creator<PaymentComponentState>() {
        public PaymentComponentState createFromParcel(@NonNull Parcel in) {
            return new PaymentComponentState(in);
        }

        public PaymentComponentState[] newArray(int size) {
            return new PaymentComponentState[size];
        }
    };

    private final PaymentComponentData<PaymentMethodDetailsT> mPaymentComponentData;
    private final boolean mIsValid;

    public PaymentComponentState(@NonNull PaymentComponentData<PaymentMethodDetailsT> paymentComponentData, boolean isValid) {
        mPaymentComponentData = paymentComponentData;
        mIsValid = isValid;
    }

    PaymentComponentState(@NonNull Parcel in) {
        mPaymentComponentData = in.readParcelable(PaymentComponentData.class.getClassLoader());
        mIsValid = ParcelUtils.readBoolean(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(mPaymentComponentData, flags);
        ParcelUtils.writeBoolean(dest, mIsValid);
    }

    @Override
    public int describeContents() {
        return ParcelUtils.NO_FILE_DESCRIPTOR;
    }

    /**
     * @return The data that was collected by the component.
     */
    @NonNull
    public PaymentComponentData<PaymentMethodDetailsT> getData() {
        return mPaymentComponentData;
    }

    /**
     * @return If the collected data is valid to be sent to the backend.
     */
    public boolean isValid() {
        return mIsValid;
    }
}
