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
public interface PaymentComponent<ComponentStateT extends PaymentComponentState<? extends PaymentMethodDetails>,
        ConfigurationT extends Configuration>
        extends Component<ComponentStateT, ConfigurationT> {

    /**
     * @return An array of the supported {@link com.adyen.checkout.base.util.PaymentMethodTypes}
     */
    @NonNull
    String[] getSupportedPaymentMethodTypes();

    /**
     * @return The last {@link PaymentComponentState} of this Component.
     */
    @Nullable
    PaymentComponentState<? extends PaymentMethodDetails> getState();

    /**
     * Checks if the component in it's current configuration needs any input from the user to make the /payments call.
     *
     * @return If there is required user input or not.
     */
    boolean requiresInput();
}
