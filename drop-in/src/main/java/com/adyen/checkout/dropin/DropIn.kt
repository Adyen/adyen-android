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
import com.adyen.checkout.dropin.DropIn.Companion.startPayment
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
class DropIn private constructor() {

    companion object {
        private val TAG = LogUtil.getTag()

        const val RESULT_KEY = "payment_result"
        const val ERROR_REASON_KEY = "error_reason"
        const val ERROR_REASON_USER_CANCELED = "Canceled by user"

        const val DROP_IN_PREFS = "drop-in-shared-prefs"
        const val LOCALE_PREF = "drop-in-locale"

        const val DROP_IN_REQUEST_CODE = 529

        /**
         * Starts the checkout flow to be handled by the Drop-In solution. Make sure you have [DropInService] set up before calling this.
         * We suggest that you set up the resultHandlerIntent with the appropriate flags to clear the stack of the checkout activities.
         *
         * @param activity An activity to start the Checkout flow.
         * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
         * @param dropInConfiguration Additional required configuration data.
         *
         */
        @JvmStatic
        fun startPayment(
            activity: Activity,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            dropInConfiguration: DropInConfiguration
        ) {
            Logger.d(TAG, "startPayment from Activity")

            val intent = preparePayment(
                activity,
                paymentMethodsApiResponse,
                dropInConfiguration
            )
            activity.startActivityForResult(intent, DROP_IN_REQUEST_CODE)
        }

        /**
         * Starts the checkout flow to be handled by the Drop-In solution. Make sure you have [DropInService] set up before calling this.
         * We suggest that you set up the resultHandlerIntent with the appropriate flags to clear the stack of the checkout activities.
         *
         * @param fragment An Fragment to start the Checkout flow.
         * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
         * @param dropInConfiguration Additional required configuration data.
         *
         */
        @JvmStatic
        fun startPayment(
            fragment: Fragment,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            dropInConfiguration: DropInConfiguration
        ) {
            Logger.d(TAG, "startPayment from Fragment")

            val intent = preparePayment(
                fragment.requireContext(),
                paymentMethodsApiResponse,
                dropInConfiguration
            )
            fragment.startActivityForResult(intent, DROP_IN_REQUEST_CODE)
        }

        private fun preparePayment(
            context: Context,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            dropInConfiguration: DropInConfiguration
        ): Intent {
            // Add locale to prefs
            context.getSharedPreferences(DROP_IN_PREFS, Context.MODE_PRIVATE).edit()
                .putString(LOCALE_PREF, dropInConfiguration.shopperLocale.toString())
                .apply()

            return DropInActivity.createIntent(context, dropInConfiguration, paymentMethodsApiResponse)
        }
    }
}
