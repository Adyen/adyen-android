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
import com.adyen.checkout.dropin.DropIn.startPayment
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.SessionDropInService
import com.adyen.checkout.dropin.ui.DropInActivity
import com.adyen.checkout.sessions.model.Session

/**
 * Drop-in is our pre-built checkout UI for accepting payments. You only need to provide
 * the /paymentMethods response and some configuration data - Drop-in will handle the rest of the
 * payment flow.
 *
 * To start the payment flow, first extend the [DropInService] class, and add it to your manifest
 * file. Then call [startPayment].
 */
object DropIn {
    private val TAG = LogUtil.getTag()

    internal const val RESULT_KEY = "payment_result"
    internal const val ERROR_REASON_KEY = "error_reason"
    internal const val ERROR_REASON_USER_CANCELED = "Canceled by user"
    internal const val FINISHED_WITH_ACTION = "finish_with_action"

    /**
     * Register your Activity or Fragment with the Activity Result API and receive the final
     * Drop-in result using the [DropInCallback].
     *
     * This *must* be called unconditionally, as part of initialization path, typically as a field
     * initializer of an Activity or Fragment.
     *
     * @param caller The class that needs to launch Drop-in and receive its callback.
     * @param callback Callback for the Drop-in result.
     *
     * @return The launcher that can be used to start Drop-in.
     */
    @JvmStatic
    fun registerForDropInResult(
        caller: ActivityResultCaller,
        callback: DropInCallback
    ): ActivityResultLauncher<Intent> {
        return caller.registerForActivityResult(DropInResultContract(), callback::onDropInResult)
    }

    /**
     * Starts the checkout flow to be handled by the Drop-in solution.
     * Make sure you have [DropInService] set up before calling this.
     * Call [registerForDropInResult] to create a launcher when initializing your Activity.
     *
     * You will receive the Drop-in result in the [DropInCallback] parameter specified when
     * calling [registerForDropInResult].
     *
     * 3 states can occur from this operation:
     * - Cancelled by user: the user dismissed the Drop-in before it has completed.
     * - Error: a [DropInServiceResult.Error] was returned in the [DropInService], or an error
     * has occurred.
     * - Finished: a [DropInServiceResult.Finished] was returned in the [DropInService].
     *
     * You should always handle the cases of cancellation and error in [DropInCallback.onDropInResult].
     *
     * @param context The context to start the Checkout flow with.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
     * @param dropInConfiguration Additional required configuration data.
     * @param serviceClass Service that extends from [DropInService] that would handle network requests.
     */
    @JvmStatic
    fun startPayment(
        context: Context,
        dropInLauncher: ActivityResultLauncher<Intent>,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        serviceClass: Class<out DropInService>,
    ) {
        updateDefaultLogcatLevel(context)
        Logger.d(TAG, "startPayment from Activity")

        DropInPrefs.setShopperLocale(context, dropInConfiguration.shopperLocale)

        val intent = DropInActivity.createIntent(
            context,
            dropInConfiguration,
            paymentMethodsApiResponse,
            getComponentName(context, serviceClass),
        )
        dropInLauncher.launch(intent)
    }

    /**
     * TODO: Update this documentation
     *
     * Starts the checkout flow to be handled by the Drop-in solution.
     * Call [registerForDropInResult] to create a launcher when initializing your Activity.
     *
     * You will receive the Drop-in result in the [DropInCallback] parameter specified when
     * calling [registerForDropInResult].
     *
     * 3 states can occur from this operation:
     * - Cancelled by user: the user dismissed the Drop-in before it has completed.
     * - Error: a [DropInServiceResult.Error] was returned in the [DropInService], or an error
     * has occurred.
     * - Finished: a [DropInServiceResult.Finished] was returned in the [DropInService].
     *
     * You should always handle the cases of cancellation and error in [DropInCallback.onDropInResult].
     *
     * @param context The context to start the Checkout flow with.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param session The result from the session/ endpoint.
     * @param dropInConfiguration Additional required configuration data.
     * @param serviceClass Optional service that extends from [SessionDropInService] that would handle network requests.
     */
    @JvmStatic
    fun startPaymentWithSession(
        context: Context,
        dropInLauncher: ActivityResultLauncher<Intent>,
        session: Session,
        dropInConfiguration: DropInConfiguration,
        serviceClass: Class<out SessionDropInService> = SessionDropInService::class.java,
    ) {
        updateDefaultLogcatLevel(context)
        Logger.d(TAG, "startPayment from Activity")

        DropInPrefs.setShopperLocale(context, dropInConfiguration.shopperLocale)

        val intent = DropInActivity.createIntent(
            context,
            dropInConfiguration,
            session,
            getComponentName(context, serviceClass),
        )
        dropInLauncher.launch(intent)
    }

    private fun updateDefaultLogcatLevel(context: Context) {
        Logger.updateDefaultLogcatLevel(BuildUtils.isDebugBuild(context))
    }

    private fun getComponentName(context: Context, serviceClass: Class<*>) = ComponentName(context, serviceClass)

    /**
     * Helper method to fetch the Drop-in result string from the result intent provided to
     * [startPayment].
     *
     * Returns the value of [DropInServiceResult.Finished.result] or [null] if the intent does
     * not correspond to the Drop-in.
     *
     * @return the result of a finished Drop-in
     */
    @JvmStatic
    fun getDropInResultFromIntent(intent: Intent): String? {
        return intent.getStringExtra(RESULT_KEY)
    }
}
