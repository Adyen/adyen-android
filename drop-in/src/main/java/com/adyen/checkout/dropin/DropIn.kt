/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/3/2019.
 */

package com.adyen.checkout.dropin

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.BuildUtils
import com.adyen.checkout.dropin.DropIn.startPayment
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.ui.DropInActivity

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

    const val DROP_IN_REQUEST_CODE = 529

    const val RESULT_KEY = "payment_result"
    const val ERROR_REASON_KEY = "error_reason"
    const val ERROR_REASON_USER_CANCELED = "Canceled by user"
    const val FINISHED_WITH_ACTION = "finish_with_action"

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
    fun registerForDropInResult(caller: ActivityResultCaller, callback: DropInCallback): ActivityResultLauncher<Intent> {
        return caller.registerForActivityResult(DropInResultContract(), callback::onDropInResult)
    }

    /**
     * Starts the checkout flow to be handled by the Drop-in solution.
     * Make sure you have [DropInService] set up before calling this.
     * Call [registerForDropInResult] to create a launcher when initializing your Activity.
     * You can pass a [resultHandlerIntent] that will be launched after the Drop-in has completed
     * without any errors.
     * We suggest that you set up the [resultHandlerIntent] with the appropriate flags to clear
     * the stack of the checkout activities.
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
     * As for the Drop-in finished case, if you did not specify a [resultHandlerIntent], you will
     * also receive the result in [DropInCallback.onDropInResult].
     * However, if you do specify a [resultHandlerIntent], [DropInCallback.onDropInResult] will not
     * receive the result. Instead, that [resultHandlerIntent] will be launched when the
     * payment is finished and will contain the result. You can use the
     * [getDropInResultFromIntent] helper method to get it or you can find it in the intent
     * extras with key [RESULT_KEY].
     *
     * @param activity An activity to start the Checkout flow.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
     * @param dropInConfiguration Additional required configuration data.
     * @param resultHandlerIntent Intent to be called after Drop-in has finished.
     *
     */
    @JvmStatic
    fun startPayment(
        activity: Activity,
        dropInLauncher: ActivityResultLauncher<Intent>,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        resultHandlerIntent: Intent? = null
    ) {
        updateDefaultLogcatLevel(activity)
        Logger.d(TAG, "startPayment from Activity")

        val intent = preparePayment(
            activity,
            paymentMethodsApiResponse,
            dropInConfiguration,
            resultHandlerIntent
        )
        dropInLauncher.launch(intent)
    }

    /**
     * Starts the checkout flow to be handled by the Drop-in solution.
     * Make sure you have [DropInService] set up before calling this.
     * Call [registerForDropInResult] to create a launcher when initializing your Fragment.
     * You can pass a [resultHandlerIntent] that will be launched after the Drop-in has completed
     * without any errors.
     * We suggest that you set up the [resultHandlerIntent] with the appropriate flags to clear
     * the stack of the checkout activities.
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
     * As for the Drop-in finished case, if you did not specify a [resultHandlerIntent], you will
     * also receive the result in [DropInCallback.onDropInResult].
     * However, if you do specify a [resultHandlerIntent], [DropInCallback.onDropInResult] will not
     * receive the result. Instead, that [resultHandlerIntent] will be launched when the
     * payment is finished and will contain the result. You can use the
     * [getDropInResultFromIntent] helper method to get it or you can find it in the intent
     * extras with key [RESULT_KEY].
     *
     * @param fragment A fragment to start the Checkout flow.
     * @param dropInLauncher A launcher to start Drop-in, obtained with [registerForDropInResult].
     * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
     * @param dropInConfiguration Additional required configuration data.
     * @param resultHandlerIntent Intent to be called after Drop-in has finished.
     *
     */
    @JvmStatic
    fun startPayment(
        fragment: Fragment,
        dropInLauncher: ActivityResultLauncher<Intent>,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        resultHandlerIntent: Intent? = null
    ) {
        updateDefaultLogcatLevel(fragment.requireContext())
        Logger.d(TAG, "startPayment from Fragment")

        val intent = preparePayment(
            fragment.requireContext(),
            paymentMethodsApiResponse,
            dropInConfiguration,
            resultHandlerIntent
        )
        dropInLauncher.launch(intent)
    }

    /**
     * Starts the checkout flow to be handled by the Drop-in solution.
     * Make sure you have [DropInService] set up before calling this.
     * You can pass a [resultHandlerIntent] that will be launched after the Drop-in has completed
     * without any errors.
     * We suggest that you set up the [resultHandlerIntent] with the appropriate flags to clear
     * the stack of the checkout activities.
     *
     * 3 states can occur from this operation:
     * - Cancelled by user: the user dismissed the Drop-in before it has completed.
     * - Error: a [DropInServiceResult.Error] was returned in the [DropInService], or an error
     * has occurred.
     * - Finished: a [DropInServiceResult.Finished] was returned in the [DropInService].
     *
     * You should always handle the cases of cancellation and error in [Activity.onActivityResult]
     * (request code [DROP_IN_REQUEST_CODE]).
     * You can make use of the [handleActivityResult] helper method to get a [DropInResult] object.
     * If you prefer to handle the activity result manually, you should expect an
     * [Activity.RESULT_CANCELED] result code (for both error and cancellation). The data
     * intent will contain the error reason extra with key [ERROR_REASON_KEY]. Its value will be
     * [ERROR_REASON_USER_CANCELED] in case of user cancellation or the error reason otherwise.
     *
     * As for the Drop-in finished case, if you did not specify a [resultHandlerIntent], you will
     * also receive the result in [Activity.onActivityResult]. You can make use of the
     * [handleActivityResult] helper method. If you prefer to handle the activity result
     * manually, you should expect an [Activity.RESULT_OK] result code. The data intent will
     * contain the result string extra with key [RESULT_KEY] and will hold the same value as the
     * [DropInServiceResult.Finished.result] returned inside the [DropInService].
     *
     * However, if you do specify a [resultHandlerIntent], [Activity.onActivityResult] will not
     * receive the result. Instead, that [resultHandlerIntent] will be launched when the
     * payment is finished and will contain the result. You can use the
     * [getDropInResultFromIntent] helper method to get it or you can find it in the intent
     * extras with key [RESULT_KEY].
     *
     * @param activity An activity to start the Checkout flow.
     * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
     * @param dropInConfiguration Additional required configuration data.
     * @param resultHandlerIntent Intent to be called after Drop-in has finished.
     *
     */
    @JvmStatic
    fun startPayment(
        activity: Activity,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        resultHandlerIntent: Intent? = null
    ) {
        updateDefaultLogcatLevel(activity)
        Logger.d(TAG, "startPayment from Activity")

        val intent = preparePayment(
            activity,
            paymentMethodsApiResponse,
            dropInConfiguration,
            resultHandlerIntent
        )
        activity.startActivityForResult(intent, DROP_IN_REQUEST_CODE)
    }

    /**
     * Starts the checkout flow to be handled by the Drop-in solution.
     * Make sure you have [DropInService] set up before calling this.
     * You can pass a [resultHandlerIntent] that will be launched after the Drop-in has completed
     * without any errors.
     * We suggest that you set up the [resultHandlerIntent] with the appropriate flags to clear
     * the stack of the checkout activities.
     *
     * 3 states can occur from this operation:
     * - Cancelled by user: the user dismissed the Drop-in before it has completed.
     * - Error: a [DropInServiceResult.Error] was returned in the [DropInService], or an error
     * has occurred.
     * - Finished: a [DropInServiceResult.Finished] was returned in the [DropInService].
     *
     * You should always handle the cases of cancellation and error in [Fragment.onActivityResult]
     * (request code [DROP_IN_REQUEST_CODE]).
     * You can make use of the [handleActivityResult] helper method to get a [DropInResult] object.
     * If you prefer to handle the activity result manually, you should expect an
     * [Activity.RESULT_CANCELED] result code (for both error and cancellation). The data
     * intent will contain the error reason extra with key [ERROR_REASON_KEY]. Its value will be
     * [ERROR_REASON_USER_CANCELED] in case of user cancellation or the error reason otherwise.
     *
     * As for the Drop-in finished case, if you did not specify a [resultHandlerIntent], you will
     * also receive the result in [Fragment.onActivityResult]. You can make use of the
     * [handleActivityResult] helper method. If you prefer to handle the activity result
     * manually, you should expect an [Activity.RESULT_OK] result code. The data intent will
     * contain the result string extra with key [RESULT_KEY] and will hold the same value as the
     * [DropInServiceResult.Finished.result] returned inside the [DropInService].
     *
     * However, if you do specify a [resultHandlerIntent], [Fragment.onActivityResult] will not
     * receive the result. Instead, that [resultHandlerIntent] will be launched when the
     * payment is finished and will contain the result. You can use the
     * [getDropInResultFromIntent] helper method to get it or you can find it in the intent
     * extras with key [RESULT_KEY].
     *
     * @param fragment A fragment to start the Checkout flow.
     * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
     * @param dropInConfiguration Additional required configuration data.
     * @param resultHandlerIntent Intent to be called after Drop-in has finished.
     *
     */
    @JvmStatic
    fun startPayment(
        fragment: Fragment,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        resultHandlerIntent: Intent? = null
    ) {
        updateDefaultLogcatLevel(fragment.requireContext())
        Logger.d(TAG, "startPayment from Fragment")

        val intent = preparePayment(
            fragment.requireContext(),
            paymentMethodsApiResponse,
            dropInConfiguration,
            resultHandlerIntent
        )
        fragment.startActivityForResult(intent, DROP_IN_REQUEST_CODE)
    }

    private fun preparePayment(
        context: Context,
        paymentMethodsApiResponse: PaymentMethodsApiResponse,
        dropInConfiguration: DropInConfiguration,
        resultHandlerIntent: Intent?
    ): Intent {
        // Add locale to prefs
        DropInPrefs.setShopperLocale(context, dropInConfiguration.shopperLocale)

        return DropInActivity.createIntent(
            context,
            dropInConfiguration,
            paymentMethodsApiResponse,
            resultHandlerIntent
        )
    }

    private fun updateDefaultLogcatLevel(context: Context) {
        Logger.updateDefaultLogcatLevel(BuildUtils.isDebugBuild(context))
    }

    /**
     * Helper method to transform the activity result into a [DropInResult].
     *
     * The returned value could be:
     * * [DropInResult.CancelledByUser] if the operation was cancelled by the user.
     * * [DropInResult.Error] if an unexpected error has occurred during Drop-in, the
     * [DropInResult.Error.reason] field will contain the error detail.
     * * [DropInResult.Error] if a [DropInServiceResult.Error] was returned to the
     * [DropInService]. The [DropInResult.Error.reason] field will hold the same value as
     * [DropInServiceResult.Error.reason].
     * * [DropInResult.Finished] if a [DropInServiceResult.Finished] was returned to the
     * [DropInService]. The [DropInResult.Finished.result] field will hold the same value as
     * [DropInServiceResult.Finished.result].
     * * [null] if the activity result does not correspond to the Drop-in.
     *
     * Note that [DropInResult.Finished] will be returned here only if a result intent was
     * provided to the [startPayment] method.
     *
     * @return the result of the Drop-in.
     *
     */
    @JvmStatic
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): DropInResult? {
        return when {
            requestCode != DROP_IN_REQUEST_CODE || data == null -> null
            resultCode == Activity.RESULT_CANCELED && data.hasExtra(ERROR_REASON_KEY) -> {
                val reason = data.getStringExtra(ERROR_REASON_KEY) ?: ""
                if (reason == ERROR_REASON_USER_CANCELED) DropInResult.CancelledByUser()
                else DropInResult.Error(reason)
            }
            resultCode == Activity.RESULT_OK && data.hasExtra(RESULT_KEY) -> {
                DropInResult.Finished(data.getStringExtra(RESULT_KEY) ?: "")
            }
            else -> null
        }
    }

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
