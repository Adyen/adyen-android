/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/3/2019.
 */

package com.adyen.checkout.dropin

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.BuildUtils
import com.adyen.checkout.dropin.DropIn.registerForDropInResult
import com.adyen.checkout.dropin.DropIn.startPayment
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.SessionDropInService
import com.adyen.checkout.dropin.ui.DropInActivity
import com.adyen.checkout.sessions.CheckoutSession
import com.adyen.checkout.sessions.provider.CheckoutSessionProvider

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
    private val TAG = LogUtil.getTag()

    internal const val RESULT_KEY = "payment_result"
    internal const val ERROR_REASON_KEY = "error_reason"
    internal const val ERROR_REASON_USER_CANCELED = "Canceled by user"
    internal const val FINISHED_WITH_ACTION = "finish_with_action"

    /**
     * Register your Activity or Fragment with the Activity Result API and receive the final Drop-in result using the
     * [DropInCallback].
     *
     * This *must* be called unconditionally, as part of the initialization path, typically as a field initializer of an
     * Activity or Fragment.
     *
     * You will receive the Drop-in result in the [DropInCallback.onDropInResult] method. This method can return one of
     * these three results:
     * - Finished: a [DropInServiceResult.Finished] was returned in the [DropInService].
     * - Cancelled by user: the user dismissed the Drop-in before it has completed.
     * - Error: a [DropInServiceResult.Error] was returned in the [DropInService], or an error has occurred.
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
    ): ActivityResultLauncher<Intent> {
        return caller.registerForActivityResult(DropInResultContract(), callback::onDropInResult)
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
        dropInLauncher: ActivityResultLauncher<Intent>,
        checkoutSession: CheckoutSession,
        dropInConfiguration: DropInConfiguration,
        serviceClass: Class<out SessionDropInService> = SessionDropInService::class.java,
    ) {
        Logger.d(TAG, "startPayment with sessions")
        val intent = DropInActivity.createIntent(
            context,
            dropInConfiguration,
            checkoutSession,
            getComponentName(context, serviceClass),
        )
        startPayment(context, dropInLauncher, dropInConfiguration, intent)
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
        dropInLauncher: ActivityResultLauncher<Intent>,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        serviceClass: Class<out DropInService>,
    ) {
        Logger.d(TAG, "startPayment with payment methods")
        val intent = DropInActivity.createIntent(
            context,
            dropInConfiguration,
            paymentMethodsApiResponse,
            getComponentName(context, serviceClass),
        )
        startPayment(context, dropInLauncher, dropInConfiguration, intent)
    }

    private fun startPayment(
        context: Context,
        dropInLauncher: ActivityResultLauncher<Intent>,
        dropInConfiguration: DropInConfiguration,
        intent: Intent,
    ) {
        updateDefaultLogcatLevel(context)
        DropInPrefs.setShopperLocale(context, dropInConfiguration.shopperLocale)
        dropInLauncher.launch(intent)
    }

    private fun updateDefaultLogcatLevel(context: Context) {
        Logger.updateDefaultLogcatLevel(BuildUtils.isDebugBuild(context))
    }

    private fun getComponentName(context: Context, serviceClass: Class<*>) = ComponentName(context, serviceClass)
}
