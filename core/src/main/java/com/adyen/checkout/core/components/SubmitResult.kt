/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.components.data.OrderResponse
import com.adyen.checkout.core.action.data.Action as ActionResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods

// TODO - KDocs, revisit later after having parameters
/**
 * Represents the outcome of a checkout operation.
 */
sealed interface SubmitResult {

    /** Indicates the payment process has finished successfully. */
    // TODO - Replace temp parameter with actual value
    data class Finished(val temp: String? = null) : SubmitResult

    /** Indicates that an additional action is required from the shopper. */
    data class Action(val action: ActionResponse) : SubmitResult

    data class PartialPayment(
        val order: OrderResponse?,
        val paymentMethods: PaymentMethods,
    ) : SubmitResult

    /** Indicates an error occurred during the payment process. */
    data class Error(val error: CheckoutError) : SubmitResult
}
