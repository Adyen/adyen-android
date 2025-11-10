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
import com.adyen.checkout.core.components.Checkout.Result
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.components.internal.CheckoutInitializer
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.dropin.internal.ui.DropInActivity

@Suppress("UnusedReceiverParameter")
suspend fun Checkout.initialize(
    sessionModel: SessionModel,
    checkoutConfiguration: CheckoutConfiguration,
): Result<CheckoutDropInContext.Sessions> {
    val initializationData = CheckoutInitializer.initialize(
        checkoutConfiguration = checkoutConfiguration,
        sessionModel = sessionModel,
    )

    return when (val session = initializationData.checkoutSession) {
        null -> {
            Result.Error("Failed to initialize sessions.")
        }

        else -> Result.Success(
            checkoutContext = CheckoutDropInContext.Sessions(
                checkoutSession = session,
                checkoutConfiguration = checkoutConfiguration,
                publicKey = initializationData.publicKey,
            ),
        )
    }
}

@Suppress("UnusedReceiverParameter")
suspend fun Checkout.initialize(
    paymentMethodsApiResponse: PaymentMethodsApiResponse,
    checkoutConfiguration: CheckoutConfiguration,
): Result<CheckoutDropInContext.Advanced> {
    val initializationData = CheckoutInitializer.initialize(
        checkoutConfiguration = checkoutConfiguration,
        sessionModel = null,
    )

    return Result.Success(
        checkoutContext = CheckoutDropInContext.Advanced(
            paymentMethodsApiResponse = paymentMethodsApiResponse,
            checkoutConfiguration = checkoutConfiguration,
            publicKey = initializationData.publicKey,
        ),
    )
}

// TODO - Remove after migrating the example implementation
@Suppress("UnusedReceiverParameter", "UnusedParameter")
fun Checkout.startDropIn(
    activity: Activity,
    sessionModel: SessionModel,
    checkoutConfiguration: CheckoutConfiguration,
) {
    startDropInActivity(activity)
}

// TODO - Remove after migrating the example implementation
@Suppress("UnusedReceiverParameter", "UnusedParameter")
fun Checkout.startDropIn(
    activity: Activity,
    paymentMethodsApiResponse: PaymentMethodsApiResponse,
    checkoutConfiguration: CheckoutConfiguration,
) {
    startDropInActivity(activity)
}

private fun startDropInActivity(activity: Activity) {
    val intent = Intent(activity, DropInActivity::class.java)
    activity.startActivity(intent)
}
