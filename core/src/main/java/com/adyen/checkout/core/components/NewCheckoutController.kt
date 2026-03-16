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
import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.internal.CheckoutControllerState
import com.adyen.checkout.core.components.internal.ui.NewPaymentComponent
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.error.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO - rename later
interface CheckoutControllerInterface {
    fun validate(): Boolean
    fun submit()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NewCheckoutController(
    private val target: CheckoutTarget,
    private val context: CheckoutContext,
    private val callbacks: CheckoutCallbacks,
    private val coroutineScope: CoroutineScope,
) : CheckoutControllerInterface {

    private val _state = MutableStateFlow(createInitialState())
    internal val state: StateFlow<CheckoutControllerState> = _state.asStateFlow()

    private var paymentComponent: NewPaymentComponent? = null

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

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun attach(component: NewPaymentComponent) {
        paymentComponent = component
    }

    override fun validate(): Boolean {
        return paymentComponent?.validate() ?: false
    }

    // TODO - Support sessions
    override fun submit() {
        val component = paymentComponent

        if (component == null) {
            val error = CheckoutError(
                code = CheckoutError.ErrorCode.UNKNOWN,
                message = "Submit should not be called when there is no component attached",
            )
            callbacks.onError?.onError(error)
            return
        }

        if (validate()) {
            coroutineScope.launch {
                val componentState = component.getState()
                component.setLoading(true)
                callbacks.onSubmit?.onSubmit(componentState)
                component.setLoading(false)
            }
        }
    }
}
