/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin

import android.app.Activity
import android.content.Intent
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.dropin.internal.ui.DropInActivity

// TODO - Add launcher and service
@Suppress("UnusedReceiverParameter", "UnusedParameter")
fun Checkout.startDropIn(
    activity: Activity,
    sessionModel: SessionModel,
    checkoutConfiguration: CheckoutConfiguration,
) {
    // TODO - Call Checkout.initialize internally
    startDropInActivity(activity)
}

// TODO - Add launcher and service
@Suppress("UnusedReceiverParameter", "UnusedParameter")
fun Checkout.startDropIn(
    activity: Activity,
    paymentMethodsApiResponse: PaymentMethodsApiResponse,
    checkoutConfiguration: CheckoutConfiguration,
) {
    // TODO - Call Checkout.initialize internally
    startDropInActivity(activity)
}

private fun startDropInActivity(activity: Activity) {
    val intent = Intent(activity, DropInActivity::class.java)
    activity.startActivity(intent)
}
