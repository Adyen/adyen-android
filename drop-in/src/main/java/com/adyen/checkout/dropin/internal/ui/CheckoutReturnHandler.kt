/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Intent
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog

/**
 * Delivers the intent received when the shopper returns from an external redirect to the payment flow
 * that is handling the action.
 *
 * The displayed action screen determines the target: only that flow can be waiting for a return.
 */
internal class CheckoutReturnHandler(
    private val navigator: DropInNavigator,
    private val flowHolder: CheckoutFlowHolder,
) {

    fun handle(intent: Intent) {
        val paymentFlowType = (navigator.currentKey as? ActionNavKey)?.paymentFlowType
        if (paymentFlowType == null) {
            adyenLog(AdyenLogLevel.WARN) { "Ignoring return intent, no action is being displayed" }
            return
        }

        val controller = flowHolder.peekController(paymentFlowType)
        if (controller == null) {
            adyenLog(AdyenLogLevel.WARN) { "Ignoring return intent, $paymentFlowType has no controller" }
            return
        }

        controller.handleReturn(intent)
    }
}
