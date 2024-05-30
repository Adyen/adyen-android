/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/3/2019.
 */

package com.adyen.checkout.dropin

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.AdyenLogger
import com.adyen.checkout.core.internal.util.BuildUtils
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.DropIn.registerForDropInResult
import com.adyen.checkout.dropin.DropIn.startPayment
import com.adyen.checkout.dropin.internal.ui.model.DropInParamsMapper
import com.adyen.checkout.dropin.internal.ui.model.DropInResultContractParams
import com.adyen.checkout.dropin.internal.ui.model.SessionDropInResultContractParams
import com.adyen.checkout.dropin.internal.util.DropInPrefs
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.CheckoutSessionProvider
import com.adyen.checkout.sessions.core.internal.ui.model.SessionParamsFactory
import java.util.Locale

/**
 * Drop-in is our pre-built checkout UI for accepting payments. You only need to integrate through your backend with the
 * /sessions API endpoint and provide some configuration data. Drop-in will handle the rest of the checkout flow.
 *
 * Alternatively you can opt for a more advanced implementation with the 3 main API endpoints: /paymentMethods,
 * /payments and /payments/details.
 *
 * To start the checkout flow, register you activity or fragment using [registerForDropInResult] to receive the result
 * of Drop-in. Then call one of the [startPayment] methods.
 */
object DropIn {

    internal const val RESULT_KEY = "payment_result"
    internal const val SESSION_RESULT_KEY = "session_payment_result"
    internal const val ERROR_REASON_KEY = "error_reason"
    internal const val ERROR_REASON_USER_CANCELED = "Canceled by user"
    internal const val FINISHED_WITH_ACTION = "finish_with_action"

    /**
     * Register your Activity or Fragment with the Activity Result API and receive the final Drop-in result using the
     * [SessionDropInCallback].
     *
     * This *must* be called unconditionally, as part of the initialization path, typically as a field initializer of an
     * Activity or Fragment.
     *
     * You will receive the Drop-in result in the [SessionDropInCallback.onDropInResult] method. Check out
     * [SessionDropInResult] class for all the possible results you might receive.
     *
     * @param caller The class that needs to launch Drop-in and receive its callback.
     * @param callback Callback for the Drop-in result.
     *
     * @return The [ActivityResultLauncher] required to receive the result of Drop-in.
     */
    @JvmStatic
    fun registerForDropInResult(
        caller: ActivityResultCaller,
        callback: SessionDropInCallback
    ): ActivityResultLauncher<SessionDropInResultContractParams> {
        return caller.registerForActivityResult(SessionDropInResultContract(), callback::onDropInResult)
    }

    /**
     * Starts the checkout flow to be handled by the Drop-in solution. With this solution your backend only needs to
     * integrate the /sessions endpoint to start the checkout flow.
     *
     * Call [registerForDropInResult] to create a launcher when initializing your Activity or Fragment and receive the
     * final result of Drop-in.
     *
     * Use [dropInConfiguration] to configure Drop-in and the components that will be loaded inside it.
     *
     * Optionally, you can extend [SessionDropInService] with your own implementation and add it to your manifest file.
     * This allows you to interact with Drop-in, and take over the checkout flow.
     *
     * @param context The context to start the Checkout flow with.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param checkoutSession The result from the /sessions endpoint passed onto [CheckoutSessionProvider.createSession]
     * to create this object.
     * @param dropInConfiguration Additional required configuration data.
     * @param serviceClass Service that extends from [SessionDropInService] to optionally take over the checkout flow.
     */
    @JvmStatic
    fun startPayment(
        context: Context,
        dropInLauncher: ActivityResultLauncher<SessionDropInResultContractParams>,
        checkoutSession: CheckoutSession,
        dropInConfiguration: DropInConfiguration,
        serviceClass: Class<out SessionDropInService> = SessionDropInService::class.java,
    ) {
        startPayment(
            context = context,
            dropInLauncher = dropInLauncher,
            checkoutSession = checkoutSession,
            checkoutConfiguration = dropInConfiguration.toCheckoutConfiguration(),
            serviceClass = serviceClass,
        )
    }

    /**
     * Starts the checkout flow to be handled by the Drop-in solution. With this solution your backend only needs to
     * integrate the /sessions endpoint to start the checkout flow.
     *
     * Call [registerForDropInResult] to create a launcher when initializing your Activity or Fragment and receive the
     * final result of Drop-in.
     *
     * Use [checkoutConfiguration] to configure Drop-in and the components that will be loaded inside it.
     *
     * Optionally, you can extend [SessionDropInService] with your own implementation and add it to your manifest file.
     * This allows you to interact with Drop-in, and take over the checkout flow.
     *
     * @param context The context to start the Checkout flow with.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param checkoutSession The result from the /sessions endpoint passed onto [CheckoutSessionProvider.createSession]
     * to create this object.
     * @param checkoutConfiguration Additional required configuration data.
     * @param serviceClass Service that extends from [SessionDropInService] to optionally take over the checkout flow.
     */
    @JvmStatic
    fun startPayment(
        context: Context,
        dropInLauncher: ActivityResultLauncher<SessionDropInResultContractParams>,
        checkoutSession: CheckoutSession,
        checkoutConfiguration: CheckoutConfiguration = checkoutSession.getConfiguration(),
        serviceClass: Class<out SessionDropInService> = SessionDropInService::class.java,
    ) {
        adyenLog(AdyenLogLevel.DEBUG) { "startPayment with sessions" }
        val sessionDropInResultContractParams = SessionDropInResultContractParams(
            checkoutConfiguration,
            checkoutSession,
            serviceClass,
        )
        val sessionParams = SessionParamsFactory.create(checkoutSession)
        val shopperLocale = DropInParamsMapper().getShopperLocale(checkoutConfiguration, sessionParams)

        startPayment(context, dropInLauncher, shopperLocale, sessionDropInResultContractParams)
    }

    /**
     * Register your Activity or Fragment with the Activity Result API and receive the final Drop-in result using the
     * [DropInCallback].
     *
     * This *must* be called unconditionally, as part of the initialization path, typically as a field initializer of an
     * Activity or Fragment.
     *
     * You will receive the Drop-in result in the [DropInCallback.onDropInResult] method. Check out [DropInResult] for
     * all the possible results you might receive.
     *
     * @param caller The class that needs to launch Drop-in and receive its callback.
     * @param callback Callback for the Drop-in result.
     *
     * @return The [ActivityResultLauncher] required to receive the result of Drop-in.
     */
    @JvmStatic
    fun registerForDropInResult(
        caller: ActivityResultCaller,
        callback: DropInCallback
    ): ActivityResultLauncher<DropInResultContractParams> {
        return caller.registerForActivityResult(DropInResultContract(), callback::onDropInResult)
    }

    /**
     * Starts the advanced checkout flow to be handled by the Drop-in solution. With this solution your backend needs to
     * integrate the 3 main API endpoints: /paymentMethods, /payments and /payments/details.
     *
     * Extend [DropInService] with your own implementation and add it to your manifest file. This class allows you to
     * interact with Drop-in during the checkout flow.
     *
     * Call [registerForDropInResult] to create a launcher when initializing your Activity or Fragment and receive the
     * final result of Drop-in.
     *
     * Use [dropInConfiguration] to configure Drop-in and the components that will be loaded inside it.
     *
     * @param context The context to start the Checkout flow with.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param paymentMethodsApiResponse The result from the /paymentMethods endpoint.
     * @param dropInConfiguration Additional required configuration data.
     * @param serviceClass Service that extends from [DropInService] to interact with Drop-in during the checkout flow.
     */
    @JvmStatic
    fun startPayment(
        context: Context,
        dropInLauncher: ActivityResultLauncher<DropInResultContractParams>,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        serviceClass: Class<out DropInService>,
    ) {
        startPayment(
            context = context,
            dropInLauncher = dropInLauncher,
            paymentMethodsApiResponse = paymentMethodsApiResponse,
            checkoutConfiguration = dropInConfiguration.toCheckoutConfiguration(),
            serviceClass = serviceClass,
        )
    }

    /**
     * Starts the advanced checkout flow to be handled by the Drop-in solution. With this solution your backend needs to
     * integrate the 3 main API endpoints: /paymentMethods, /payments and /payments/details.
     *
     * Extend [DropInService] with your own implementation and add it to your manifest file. This class allows you to
     * interact with Drop-in during the checkout flow.
     *
     * Call [registerForDropInResult] to create a launcher when initializing your Activity or Fragment and receive the
     * final result of Drop-in.
     *
     * Use [checkoutConfiguration] to configure Drop-in and the components that will be loaded inside it.
     *
     * @param context The context to start the Checkout flow with.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param paymentMethodsApiResponse The result from the /paymentMethods endpoint.
     * @param checkoutConfiguration Additional required configuration data.
     * @param serviceClass Service that extends from [DropInService] to interact with Drop-in during the checkout flow.
     */
    @JvmStatic
    fun startPayment(
        context: Context,
        dropInLauncher: ActivityResultLauncher<DropInResultContractParams>,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        checkoutConfiguration: CheckoutConfiguration,
        serviceClass: Class<out DropInService>,
    ) {
        adyenLog(AdyenLogLevel.DEBUG) { "startPayment with payment methods" }
        val dropInResultContractParams = DropInResultContractParams(
            checkoutConfiguration,
            paymentMethodsApiResponse,
            serviceClass,
        )
        val shopperLocale = DropInParamsMapper().getShopperLocale(checkoutConfiguration, null)

        startPayment(context, dropInLauncher, shopperLocale, dropInResultContractParams)
    }

    private fun <T> startPayment(
        context: Context,
        dropInLauncher: ActivityResultLauncher<T>,
        shopperLocale: Locale?,
        params: T,
    ) {
        updateDefaultLogLevel(context)
        DropInPrefs.setShopperLocale(context, shopperLocale)
        dropInLauncher.launch(params)
    }

    private fun updateDefaultLogLevel(context: Context) {
        val logLevel = if (BuildUtils.isDebugBuild(context)) {
            AdyenLogLevel.DEBUG
        } else {
            AdyenLogLevel.NONE
        }
        AdyenLogger.setInitialLogLevel(logLevel)
    }
}
