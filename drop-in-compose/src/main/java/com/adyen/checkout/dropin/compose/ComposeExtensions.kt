/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/6/2023.
 */

package com.adyen.checkout.dropin.compose

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInCallback
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResultContract
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.SessionDropInCallback
import com.adyen.checkout.dropin.SessionDropInResultContract
import com.adyen.checkout.dropin.SessionDropInService
import com.adyen.checkout.dropin.internal.ui.model.DropInResultContractParams
import com.adyen.checkout.dropin.internal.ui.model.SessionDropInResultContractParams
import com.adyen.checkout.sessions.core.CheckoutSession

// TODO docs
@Composable
fun DropIn.registerForDropInResult(
    callback: SessionDropInCallback
): ActivityResultLauncher<SessionDropInResultContractParams> {
    return rememberLauncherForActivityResult(
        contract = SessionDropInResultContract(),
        onResult = callback::onDropInResult
    )
}

// TODO docs
@SuppressLint("ComposableNaming")
@Composable
fun DropIn.startPayment(
    dropInLauncher: ActivityResultLauncher<SessionDropInResultContractParams>,
    checkoutSession: CheckoutSession,
    dropInConfiguration: DropInConfiguration,
    serviceClass: Class<out SessionDropInService> = SessionDropInService::class.java,
) {
    startPayment(
        context = LocalContext.current,
        dropInLauncher = dropInLauncher,
        checkoutSession = checkoutSession,
        dropInConfiguration = dropInConfiguration,
        serviceClass = serviceClass
    )
}

// TODO docs
@Composable
fun DropIn.registerForDropInResult(
    callback: DropInCallback
): ActivityResultLauncher<DropInResultContractParams> {
    return rememberLauncherForActivityResult(
        contract = DropInResultContract(),
        onResult = callback::onDropInResult
    )
}

// TODO docs
@SuppressLint("ComposableNaming")
@Composable
fun DropIn.startPayment(
    dropInLauncher: ActivityResultLauncher<DropInResultContractParams>,
    paymentMethodsApiResponse: PaymentMethodsApiResponse,
    dropInConfiguration: DropInConfiguration,
    serviceClass: Class<out DropInService>,
) {
    startPayment(
        context = LocalContext.current,
        dropInLauncher = dropInLauncher,
        paymentMethodsApiResponse = paymentMethodsApiResponse,
        dropInConfiguration = dropInConfiguration,
        serviceClass = serviceClass
    )
}
