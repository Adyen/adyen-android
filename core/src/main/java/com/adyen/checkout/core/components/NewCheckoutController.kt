/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.data.model.PaymentMethodResponse
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.adyen.checkout.core.action.data.Action as ActionResponse

class NewCheckoutController(
    val target: CheckoutTarget,
    val context: CheckoutContext,
    val callbacks: CheckoutCallbacks,
) {

    private val _state = MutableStateFlow(createInitialState())
    internal val state: StateFlow<CheckoutControllerState> = _state.asStateFlow()

    private fun createInitialState(): CheckoutControllerState {
        return when (target) {
            is CheckoutTarget.PaymentMethod -> {
                val paymentMethod = getPaymentMethodResponse()?.paymentMethods?.find { it.type == target.txVariant }
                CheckoutControllerState.PaymentMethod(paymentMethod)
            }

            is CheckoutTarget.StoredPaymentMethod -> {
                val paymentMethod = getPaymentMethodResponse()?.storedPaymentMethods?.find { it.id == target.id }
                CheckoutControllerState.PaymentMethod(paymentMethod)
            }

            is CheckoutTarget.Action -> CheckoutControllerState.Action(target.action)
            else -> {
                // TODO - introduce proper error code
                val error = CheckoutError(
                    code = CheckoutError.ErrorCode.UNKNOWN,
                    message = "Unknown target: $target",
                )
                throw CheckoutException(error)
            }
        }
    }

    private fun getPaymentMethodResponse(): PaymentMethodsApiResponse? {
        return when (context) {
            is CheckoutContext.Advanced -> context.paymentMethodsApiResponse
            is CheckoutContext.Sessions -> context.checkoutSession.sessionSetupResponse.paymentMethodsApiResponse
        }
    }
}

internal sealed interface CheckoutControllerState {
    data class PaymentMethod(val paymentMethod: PaymentMethodResponse?) : CheckoutControllerState
    data class Action(val action: ActionResponse) : CheckoutControllerState
}

interface CheckoutTarget {
    class PaymentMethod(val txVariant: String) : CheckoutTarget
    class StoredPaymentMethod(val id: String) : CheckoutTarget
    class Action(val action: ActionResponse) : CheckoutTarget
}
