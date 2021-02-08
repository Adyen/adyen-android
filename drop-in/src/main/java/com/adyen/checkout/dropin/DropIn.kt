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
import androidx.fragment.app.Fragment
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn.startPayment
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.ui.DropInActivity

/**
 * Drop-in is the easy solution to using components. The Merchant only needs to provide the response of the paymentMethods/ endpoint
 * and some configuration data. Then we will handle the UI flow to get all the needed payment information.
 *
 * Merchant needs to extend [DropInService] and put it in the manifest. That service is where the merchant will make the calls to the
 * server for the payments/ and payments/details/ endpoints/.
 *
 * After setting up the [DropInService], just call [startPayment] and the checkout process will start.
 */
object DropIn {
    private val TAG = LogUtil.getTag()

    const val DROP_IN_REQUEST_CODE = 529

    const val RESULT_KEY = "payment_result"
    const val ERROR_REASON_KEY = "error_reason"
    const val ERROR_REASON_USER_CANCELED = "Canceled by user"

    internal const val DROP_IN_PREFS = "drop-in-shared-prefs"
    internal const val LOCALE_PREF = "drop-in-locale"

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
        context.getSharedPreferences(DROP_IN_PREFS, Context.MODE_PRIVATE).edit()
            .putString(LOCALE_PREF, dropInConfiguration.shopperLocale.toString())
            .apply()

        return DropInActivity.createIntent(
            context,
            dropInConfiguration,
            paymentMethodsApiResponse,
            resultHandlerIntent
        )
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
