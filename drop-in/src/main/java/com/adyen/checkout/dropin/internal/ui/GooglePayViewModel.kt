/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.adyen.checkout.core.components.CheckoutController
import com.adyen.checkout.core.components.CheckoutRoute
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * Owns the [CheckoutController] that renders the Google Pay button and drives its payment flow.
 *
 * Google Pay is the exception to [PaymentMethodViewModel]: its button is part of the payment method list rather than a
 * screen behind a list item, so its controller has to exist before the shopper picks anything. It is therefore scoped
 * to the list instead of to a payment method screen.
 *
 * [PaymentMethodListNavKey] and [GooglePayActionNavKey] declare the same [GOOGLE_PAY_FLOW_CONTENT_KEY], so this view
 * model survives the navigation from the list to the action screen and the flow continues on the same controller.
 */
internal class GooglePayViewModel(
    private val paymentFlowType: DropInPaymentFlowType,
    private val navigator: DropInNavigator,
    controllerProvider: DropInControllerProvider,
) : ViewModel() {

    val controller: CheckoutController = controllerProvider.provide(paymentFlowType, viewModelScope)

    init {
        observeNavigation()
    }

    /**
     * Navigates to the Google Pay action screen when the payments call returns an action.
     *
     * [CheckoutController.navigation] has no replay, so the subscription has to be active before the shopper can tap
     * the button. [CoroutineStart.UNDISPATCHED] makes sure it is set up before this view model is handed out.
     */
    private fun observeNavigation() {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            controller.navigation.collect { route ->
                when (route) {
                    // Replacing the back stack means going back from the action cancels Drop-in, matching what the
                    // other payment methods do.
                    is CheckoutRoute.Action -> navigator.clearAndNavigateTo(GooglePayActionNavKey(paymentFlowType))
                    else -> Unit
                }
            }
        }
    }

    class Factory(
        private val paymentFlowType: DropInPaymentFlowType,
        private val navigator: DropInNavigator,
        private val controllerProvider: DropInControllerProvider,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return GooglePayViewModel(
                paymentFlowType = paymentFlowType,
                navigator = navigator,
                controllerProvider = controllerProvider,
            ) as T
        }
    }
}
