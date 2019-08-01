/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails;

/**
 * A component that handles collecting user input data. It handles validating and formatting the data for the UI.
 * A valid {@link PaymentComponentState} contains {@link PaymentMethodDetails} to help compose the payments/ call on the backend.
 *
 * <p/>
 * Should be used attached to a corresponding ComponentView to get data from.
 */
public interface PaymentComponent extends Component<PaymentComponentState> {

    /**
     * @return The "type" of payment method supported by this Component.
     */
    @NonNull
    String getPaymentMethodType();

    /**
     * @return The last {@link PaymentComponentState} of this Component.
     */
    @Nullable
    PaymentComponentState getState();
}
