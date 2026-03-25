/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/3/2026.
 */

package com.adyen.checkout.core.components

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethods
import com.adyen.checkout.core.components.internal.CheckoutControllerState
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO - rename later
interface CheckoutControllerInterface {
    suspend fun submit()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NewCheckoutController(
    private val target: CheckoutTarget,
    private val context: CheckoutContext,
    @Suppress("unused")
    private val callbacks: CheckoutCallbacks,
) : CheckoutControllerInterface {

    private val _state = MutableStateFlow(createInitialState())
    internal val state: StateFlow<CheckoutControllerState> = _state.asStateFlow()

    private var componentStateFlow: StateFlow<PaymentComponentState<*>>? = null

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

    private fun getPaymentMethodResponse(): PaymentMethods? {
        return when (context) {
            is CheckoutContext.Advanced -> context.paymentMethods
            is CheckoutContext.Sessions -> context.checkoutSession.sessionSetupResponse.paymentMethods
        }
    }

    fun registerComponentState(flow: StateFlow<PaymentComponentState<*>>) {
        componentStateFlow = flow
    }

    // TODO - Ensure state is valid, handle state being null, add validate function and support sessions
    override suspend fun submit() {
        if (_state.value is CheckoutControllerState.PaymentMethod) {
            componentStateFlow?.value?.let {
                callbacks.beforeSubmit?.beforeSubmit(it)
                callbacks.onSubmit?.onSubmit(it)
            }
        }
    }
}
