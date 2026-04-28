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
import com.adyen.checkout.core.action.data.Action as ActionResponse

/**
 * Represents the outcome of a checkout operation.
 */
sealed interface SubmitResult {

    /** Indicates the payment process has completed and carries a final `resultCode`. */
    data class Completion(val resultCode: String) : SubmitResult

    /** Indicates that an additional action is required from the shopper. */
    data class Action(val action: ActionResponse) : SubmitResult

    /**
     * Indicates the SDK should re-prompt the shopper. Loops back into the next `onSubmit()`.
     *
     * @param errorMessage Optional shopper-facing message the SDK can surface before re-prompting.
     */
    data class Retry(val errorMessage: String? = null) : SubmitResult

    /** Indicates that a partial payment has been made. */
    data class PartialPayment(
        val order: OrderResponse,
        val paymentMethods: PaymentMethods,
    ) : SubmitResult
}
