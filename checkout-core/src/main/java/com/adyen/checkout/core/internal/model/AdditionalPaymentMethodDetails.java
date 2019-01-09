/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 06/06/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.PaymentMethodDetails;

public abstract class AdditionalPaymentMethodDetails extends PaymentMethodDetails {
    public static void finalize(
            @NonNull AdditionalPaymentMethodDetails details,
            @NonNull PaymentInitiationResponse.DetailFields detailFields
    ) throws CheckoutException {
        details.finalize(detailFields);
    }

    protected AdditionalPaymentMethodDetails() {
    }

    protected AdditionalPaymentMethodDetails(@NonNull Parcel in) {
        super(in);
    }

    protected abstract void finalize(@NonNull PaymentInitiationResponse.DetailFields detailFields) throws CheckoutException;
}
