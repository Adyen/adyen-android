/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.AdvancedCheckoutResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError

internal class AdvancedComponentRequestDispatcher(
    private val callbacks: AdvancedCheckoutCallbacks,
) : SubmittableComponentRequestDispatcher {

    override suspend fun submit(data: PaymentComponentData<*>): SubmitResult {
        val result = callbacks.onSubmit(data)
        if (result is SubmitResult.Completion) {
            callbacks.onComplete(AdvancedCheckoutResult(CheckoutResultCode(result.resultCode)))
        }
        return result
    }

    override suspend fun additionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        val result = callbacks.onAdditionalDetails(data)
        if (result is AdditionalDetailsResult.Completion) {
            callbacks.onComplete(AdvancedCheckoutResult(CheckoutResultCode(result.resultCode)))
        }
        return result
    }

    override fun failure(error: CheckoutError) {
        callbacks.onFailure(error)
    }
}
