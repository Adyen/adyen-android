/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutRoute

/**
 * Translates the [CheckoutRoute]s of a payment flow into drop-in navigation.
 */
internal class CheckoutRouteHandler(
    private val navigator: DropInNavigator,
) {

    fun handle(route: CheckoutRoute, paymentFlowType: DropInPaymentFlowType) {
        when (route) {
            is CheckoutRoute.PaymentMethod -> navigateToPaymentMethod(paymentFlowType)
            is CheckoutRoute.Action -> navigateToAction(paymentFlowType)
            is CheckoutRoute.Secondary -> navigateToSecondary(paymentFlowType, route.identifier)
            else -> adyenLog(AdyenLogLevel.WARN) { "Unknown route: $route" }
        }
    }

    private fun navigateToPaymentMethod(paymentFlowType: DropInPaymentFlowType) {
        val key = PaymentMethodNavKey(paymentFlowType)
        // The payment method screen is removed from the back stack while an action is displayed
        if (!navigator.popTo(key)) {
            navigator.clearAndNavigateTo(key)
        }
    }

    private fun navigateToAction(paymentFlowType: DropInPaymentFlowType) {
        val key = ActionNavKey(paymentFlowType)
        if (navigator.currentKey == key) return

        // The shopper cannot go back to the payment method while an action is being handled, going back
        // from the action screen cancels the drop-in instead
        navigator.clearAndNavigateTo(key)
    }

    private fun navigateToSecondary(paymentFlowType: DropInPaymentFlowType, identifier: String) {
        val key = SecondaryNavKey(paymentFlowType, identifier)
        if (navigator.currentKey == key) return

        navigator.navigateTo(key)
    }
}
