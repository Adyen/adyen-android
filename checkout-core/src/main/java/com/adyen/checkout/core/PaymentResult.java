package com.adyen.checkout.core;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.PaymentResultCode;

/**
 * The {@link PaymentResult} interface describes the result of a payment.
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/05/2018.
 */
public interface PaymentResult extends Parcelable {
    /**
     * @return The {@link PaymentResultCode}.
     */
    @NonNull
    PaymentResultCode getResultCode();

    /**
     * @return The payload that can be used to retrieve further information about the result of the payment.
     * @see <a href="https://docs.adyen.com/developers/checkout/android-sdk/quick-start-android/verify-payment-result-android">
     *     Verifying a payment result</a>
     */
    @NonNull
    String getPayload();
}
