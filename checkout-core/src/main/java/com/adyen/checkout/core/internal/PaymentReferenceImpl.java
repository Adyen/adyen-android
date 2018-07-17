package com.adyen.checkout.core.internal;

import android.app.Activity;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 31/05/2018.
 */
public final class PaymentReferenceImpl implements PaymentReference {
    public static final Creator<PaymentReferenceImpl> CREATOR = new Creator<PaymentReferenceImpl>() {
        @Override
        public PaymentReferenceImpl createFromParcel(@NonNull Parcel source) {
            return new PaymentReferenceImpl(source);
        }

        @Override
        public PaymentReferenceImpl[] newArray(int size) {
            return new PaymentReferenceImpl[size];
        }
    };

    private final String mPaymentSessionUuid;

    PaymentReferenceImpl(@NonNull String paymentSessionUuid) {
        mPaymentSessionUuid = paymentSessionUuid;
    }

    private PaymentReferenceImpl(@NonNull Parcel source) {
        mPaymentSessionUuid = source.readString();
    }

    @NonNull
    @Override
    public String getUuid() {
        return mPaymentSessionUuid;
    }

    @Override
    public PaymentHandler getPaymentHandler(@NonNull Activity activity) {
        return PaymentHandlerImpl.getPaymentHandler(activity, this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mPaymentSessionUuid);
    }
}
