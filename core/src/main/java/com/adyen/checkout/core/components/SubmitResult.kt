/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.components.data.OrderResponse
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.action.data.Action as ActionResponse

/**
 * Represents the outcome of a checkout operation.
 */
sealed interface SubmitResult {

    /** Indicates the payment process has finished successfully. */
    data class Finished(val resultCode: String) : SubmitResult

    /** Indicates that an additional action is required from the shopper. */
    data class Action(val action: ActionResponse) : SubmitResult

    /** Indicates that a partial payment has been made. */
    data class PartialPayment(
        val order: OrderResponse?,
        val paymentMethods: PaymentMethods? = null,
    ) : SubmitResult

    /** Indicates an error occurred during the payment process. */
    data class Error(val error: CheckoutError) : SubmitResult
}
