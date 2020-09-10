/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */

package com.adyen.checkout.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails;

/**
 * A component that handles collecting user input data. It handles validating and formatting the data for the UI.
 * A valid {@link PaymentComponentState} contains {@link PaymentMethodDetails} to help compose the payments/ call on the backend.
 *
 * <p/>
 * Should be used attached to a corresponding ComponentView to get data from.
 */
public interface PaymentComponent<ComponentStateT extends PaymentComponentState, ConfigurationT extends Configuration>
        extends Component<ComponentStateT, ConfigurationT> {

    /**
     * @deprecated Use {@link #getSupportedPaymentMethodTypes()} instead with a list of the supported payment method types.
     *             This method will only return the first value of the list.
     *
     * @return The first value of "type" of payment method supported by this Component.
     */
    @Deprecated
    @NonNull
    String getPaymentMethodType();

    /**
     * @return An array of the supported {@link com.adyen.checkout.base.util.PaymentMethodTypes}
     */
    @NonNull
    String[] getSupportedPaymentMethodTypes();

    /**
     * @return The last {@link PaymentComponentState} of this Component.
     */
    @Nullable
    PaymentComponentState getState();
}
