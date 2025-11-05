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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.old.DropIn
import com.adyen.checkout.dropin.old.DropIn.registerForDropInResult
import com.adyen.checkout.dropin.old.DropInCallback
import com.adyen.checkout.dropin.old.DropInConfiguration
import com.adyen.checkout.dropin.old.DropInResult
import com.adyen.checkout.dropin.old.DropInResultContract
import com.adyen.checkout.dropin.old.DropInService
import com.adyen.checkout.dropin.old.SessionDropInCallback
import com.adyen.checkout.dropin.old.SessionDropInResult
import com.adyen.checkout.dropin.old.SessionDropInResultContract
import com.adyen.checkout.dropin.old.SessionDropInService
import com.adyen.checkout.dropin.old.internal.ui.model.DropInResultContractParams
import com.adyen.checkout.dropin.old.internal.ui.model.SessionDropInResultContractParams
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider

/**
 * Register your [Composable] with the Activity Result API and receive the final Drop-in result using the
 * [SessionDropInCallback].
 *
 * This *must* be called unconditionally, as part of the initialization path.
 *
 * You will receive the Drop-in result in the [SessionDropInCallback.onDropInResult] method. Check out
 * [SessionDropInResult] class for all the possible results you might receive.
 *
 * @param callback Callback for the Drop-in result.
 *
 * @return The [ActivityResultLauncher] required to receive the result of Drop-in.
 */
@Suppress("unused")
@Composable
fun rememberLauncherForDropInResult(
    callback: SessionDropInCallback
): ActivityResultLauncher<SessionDropInResultContractParams> {
    return rememberLauncherForActivityResult(
        contract = SessionDropInResultContract(),
        onResult = callback::onDropInResult,
    )
}

/**
 * Starts the checkout flow to be handled by the Drop-in solution. With this solution your backend only needs to
 * integrate the /sessions endpoint to start the checkout flow.
 *
 * Call [rememberLauncherForDropInResult] to create a launcher and receive the final result of Drop-in.
 *
 * Use [dropInConfiguration] to configure Drop-in and the components that will be loaded inside it.
 *
 * Optionally, you can extend [SessionDropInService] with your own implementation and add it to your manifest file.
 * This allows you to interact with Drop-in, and take over the checkout flow.
 *
 * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
 * @param checkoutSession The result from the /sessions endpoint passed onto [CheckoutSessionProvider.createSession]
 * to create this object.
 * @param dropInConfiguration Additional required configuration data.
 * @param serviceClass Service that extends from [SessionDropInService] to optionally take over the checkout flow.
 */
@SuppressLint("ComposableNaming")
@Suppress("unused")
@Composable
fun DropIn.startPayment(
    dropInLauncher: ActivityResultLauncher<SessionDropInResultContractParams>,
    checkoutSession: CheckoutSession,
    dropInConfiguration: DropInConfiguration,
    serviceClass: Class<out SessionDropInService> = SessionDropInService::class.java,
) {
    val currentContext = LocalContext.current
    LaunchedEffect(Unit) {
        startPayment(
            context = currentContext,
            dropInLauncher = dropInLauncher,
            checkoutSession = checkoutSession,
            dropInConfiguration = dropInConfiguration,
            serviceClass = serviceClass,
        )
    }
}

/**
 * Starts the checkout flow to be handled by the Drop-in solution. With this solution your backend only needs to
 * integrate the /sessions endpoint to start the checkout flow.
 *
 * Call [rememberLauncherForDropInResult] to create a launcher and receive the final result of Drop-in.
 *
 * Use [checkoutConfiguration] to configure Drop-in and the components that will be loaded inside it.
 *
 * Optionally, you can extend [SessionDropInService] with your own implementation and add it to your manifest file.
 * This allows you to interact with Drop-in, and take over the checkout flow.
 *
 * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
 * @param checkoutSession The result from the /sessions endpoint passed onto [CheckoutSessionProvider.createSession]
 * to create this object.
 * @param checkoutConfiguration Additional required configuration data.
 * @param serviceClass Service that extends from [SessionDropInService] to optionally take over the checkout flow.
 */
@SuppressLint("ComposableNaming")
@Suppress("unused")
@Composable
fun DropIn.startPayment(
    dropInLauncher: ActivityResultLauncher<SessionDropInResultContractParams>,
    checkoutSession: CheckoutSession,
    checkoutConfiguration: CheckoutConfiguration = checkoutSession.getConfiguration(),
    serviceClass: Class<out SessionDropInService> = SessionDropInService::class.java,
) {
    val currentContext = LocalContext.current
    LaunchedEffect(Unit) {
        startPayment(
            context = currentContext,
            dropInLauncher = dropInLauncher,
            checkoutSession = checkoutSession,
            checkoutConfiguration = checkoutConfiguration,
            serviceClass = serviceClass,
        )
    }
}

/**
 * Register your [Composable] with the Activity Result API and receive the final Drop-in result using the
 * [DropInCallback].
 *
 * This *must* be called unconditionally, as part of the initialization path.
 *
 * You will receive the Drop-in result in the [DropInCallback.onDropInResult] method. Check out [DropInResult] for
 * all the possible results you might receive.
 *
 * @param callback Callback for the Drop-in result.
 *
 * @return The [ActivityResultLauncher] required to receive the result of Drop-in.
 */
@Suppress("unused")
@Composable
fun rememberLauncherForDropInResult(
    callback: DropInCallback
): ActivityResultLauncher<DropInResultContractParams> {
    return rememberLauncherForActivityResult(
        contract = DropInResultContract(),
        onResult = callback::onDropInResult,
    )
}

/**
 * Starts the advanced checkout flow to be handled by the Drop-in solution. With this solution your backend needs to
 * integrate the 3 main API endpoints: /paymentMethods, /payments and /payments/details.
 *
 * Extend [DropInService] with your own implementation and add it to your manifest file. This class allows you to
 * interact with Drop-in during the checkout flow.
 *
 * Call [rememberLauncherForDropInResult] to create a launcher and receive the final result of Drop-in.
 *
 * Use [dropInConfiguration] to configure Drop-in and the components that will be loaded inside it.
 *
 * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
 * @param paymentMethodsApiResponse The result from the /paymentMethods endpoint.
 * @param dropInConfiguration Additional required configuration data.
 * @param serviceClass Service that extends from [DropInService] to interact with Drop-in during the checkout flow.
 */
@SuppressLint("ComposableNaming")
@Suppress("unused")
@Composable
fun DropIn.startPayment(
    dropInLauncher: ActivityResultLauncher<DropInResultContractParams>,
    paymentMethodsApiResponse: PaymentMethodsApiResponse,
    dropInConfiguration: DropInConfiguration,
    serviceClass: Class<out DropInService>,
) {
    val currentContext = LocalContext.current
    LaunchedEffect(Unit) {
        startPayment(
            context = currentContext,
            dropInLauncher = dropInLauncher,
            paymentMethodsApiResponse = paymentMethodsApiResponse,
            dropInConfiguration = dropInConfiguration,
            serviceClass = serviceClass,
        )
    }
}

/**
 * Starts the advanced checkout flow to be handled by the Drop-in solution. With this solution your backend needs to
 * integrate the 3 main API endpoints: /paymentMethods, /payments and /payments/details.
 *
 * Extend [DropInService] with your own implementation and add it to your manifest file. This class allows you to
 * interact with Drop-in during the checkout flow.
 *
 * Call [rememberLauncherForDropInResult] to create a launcher and receive the final result of Drop-in.
 *
 * Use [checkoutConfiguration] to configure Drop-in and the components that will be loaded inside it.
 *
 * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
 * @param paymentMethodsApiResponse The result from the /paymentMethods endpoint.
 * @param checkoutConfiguration Additional required configuration data.
 * @param serviceClass Service that extends from [DropInService] to interact with Drop-in during the checkout flow.
 */
@SuppressLint("ComposableNaming")
@Suppress("unused")
@Composable
fun DropIn.startPayment(
    dropInLauncher: ActivityResultLauncher<DropInResultContractParams>,
    paymentMethodsApiResponse: PaymentMethodsApiResponse,
    checkoutConfiguration: CheckoutConfiguration,
    serviceClass: Class<out DropInService>,
) {
    val currentContext = LocalContext.current
    LaunchedEffect(Unit) {
        startPayment(
            context = currentContext,
            dropInLauncher = dropInLauncher,
            paymentMethodsApiResponse = paymentMethodsApiResponse,
            checkoutConfiguration = checkoutConfiguration,
            serviceClass = serviceClass,
        )
    }
}
