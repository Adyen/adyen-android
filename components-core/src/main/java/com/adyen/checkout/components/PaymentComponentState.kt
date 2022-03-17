/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/3/2022.
 */
package com.adyen.checkout.components

import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

/**
 * The current state of a PaymentComponent.
 */
open class PaymentComponentState<PaymentMethodDetailsT : PaymentMethodDetails>(
    /**
     * @return The data that was collected by the component.
     */
    val data: PaymentComponentData<PaymentMethodDetailsT>,
    /**
     * @return If the component UI data is valid.
     */
    val isInputValid: Boolean,
    /**
     * @return If the component initialisation is done and data can be sent to the backend when valid.
     */
    val isReady: Boolean
) {

    /**
     * @return If the collected data is valid to be sent to the backend.
     */
    open val isValid: Boolean
        get() = isInputValid && isReady
}
