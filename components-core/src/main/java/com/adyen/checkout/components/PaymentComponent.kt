/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.components

import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

/**
 * A component that handles collecting user input data. It handles validating and formatting the data for the UI.
 * A valid [PaymentComponentState] contains [PaymentMethodDetails] to help compose the payments/ call on the backend.
 *
 *
 *
 * Should be used attached to a corresponding ComponentView to get data from.
 */
interface PaymentComponent<
    ComponentStateT : PaymentComponentState<out PaymentMethodDetails>,
    ConfigurationT : Configuration> :
    Component<ComponentStateT, ConfigurationT> {
    /**
     * @return An array of the supported [com.adyen.checkout.components.util.PaymentMethodTypes]
     */
    fun getSupportedPaymentMethodTypes(): Array<String>

    /**
     * @return The last [PaymentComponentState] of this Component.
     */
    val state: PaymentComponentState<out PaymentMethodDetails>?

    /**
     * Checks if the component in its current configuration needs any input from the user to make the /payments call.
     *
     * @return If there is required user input or not.
     */
    fun requiresInput(): Boolean
}
