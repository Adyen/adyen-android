/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/6/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutPaymentMethodRoute
import com.adyen.checkout.core.components.CheckoutSecondaryRoute
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutError.ErrorCode.PAYMENT_METHOD_FAILURE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal class FailureCheckoutFlow(
    errorMessage: String,
    componentRequestDispatcher: ComponentRequestDispatcher,
) : CheckoutFlow {

    override val paymentComponent: PaymentComponent? = null
    override val actionComponent: ActionComponent? = null
    override val paymentMethodNavigation: Flow<CheckoutPaymentMethodRoute> = emptyFlow()
    override val secondaryNavigation: Flow<CheckoutSecondaryRoute> = emptyFlow()

    init {
        val error = CheckoutError(
            code = PAYMENT_METHOD_FAILURE,
            message = errorMessage,
        )
        componentRequestDispatcher.failure(error)
    }

    override fun submit() {
        adyenLog(AdyenLogLevel.WARN) { "Ignoring submit() call, flow has already failed" }
    }

    override fun handleReturn(intent: Intent) {
        adyenLog(AdyenLogLevel.WARN) { "Ignoring handleReturn() call, flow has already failed" }
    }

    override fun requiresUserInteraction(): Boolean {
        return false
    }
}
