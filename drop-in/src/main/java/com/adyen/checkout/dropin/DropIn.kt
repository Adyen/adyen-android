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
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
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

        const val DROP_IN_PREFS = "drop-in-shared-prefs"
        const val LOCALE_PREF = "drop-in-locale"

        const val DROP_IN_REQUEST_CODE = 529

        /**
         * Starts the checkout flow to be handled by the Drop-In solution. Make sure you have [DropInService] set up before calling this.
         * We suggest that you set up the resultHandlerIntent with the appropriate flags to clear the stack of the checkout activities.
         *
         * If you want to have a cancellation callback, pass an [Activity] as context and you will get activityResult as CANCELED.
         *
         * @param context A context to start the Checkout flow.
         * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
         * @param dropInConfiguration Additional required configuration data.
         *
         */
        @JvmStatic
        fun startPayment(
            context: Context,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            dropInConfiguration: DropInConfiguration
        ) {
            Logger.d(TAG, "startPayment")

            // Add locale to prefs
            context.getSharedPreferences(DROP_IN_PREFS, Context.MODE_PRIVATE).edit()
                .putString(LOCALE_PREF, dropInConfiguration.shopperLocale.toString())
                .apply()

            val intent = DropInActivity.createIntent(context, dropInConfiguration, paymentMethodsApiResponse)
            if (context is Activity) {
                context.startActivityForResult(intent, DROP_IN_REQUEST_CODE)
            } else {
                context.startActivity(intent)
            }
        }
    }
}
