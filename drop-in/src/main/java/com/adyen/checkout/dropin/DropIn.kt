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
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.data.CardType
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
@Suppress("SyntheticAccessor")
class DropIn private constructor() {

    companion object {
        private val TAG = LogUtil.getTag()

        const val RESULT_KEY = "payment_result"

        @JvmStatic
        @Deprecated("You can use `DropIn.startPayment instead`")
        val INSTANCE: DropIn by lazy { DropIn() }

        /**
         * Starts the checkout flow to be handled by the Drop-In solution. Make sure you have [DropInService] set up before calling this.
         * We suggest that you set up the resultHandlerIntent with the appropriate flags to clear the stack of the checkout activities.
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

            for (each in paymentMethodsApiResponse.paymentMethods!!) {
                if (each.type == PaymentMethodTypes.SCHEME) {
                    this.handleSupportedCards(dropInConfiguration, each, context)
                    break
                }
            }

            val intent = DropInActivity.createIntent(context, dropInConfiguration, paymentMethodsApiResponse)
            context.startActivity(intent)
        }

        init {
            Logger.d(TAG, "Init")
        }

        /**
         * Try to get supported cards from API response when [CardConfiguration] supported cards are default ones.
         */
        @Suppress("SpreadOperator")
        private fun handleSupportedCards(dropInConfiguration: DropInConfiguration, schemePaymentMethod: PaymentMethod, context: Context) {
            var cardConfiguration = dropInConfiguration.getConfigurationFor<CardConfiguration>(PaymentMethodTypes.SCHEME, context)
            if (cardConfiguration.supportedCardTypes == CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST) {
                var supportedCardTypesFromApi = schemePaymentMethod.brands?.mapNotNull { brand -> CardType.getCardTypeByTxVariant(brand) }
                if (!supportedCardTypesFromApi.isNullOrEmpty()) {
                    val newCardConfiguration = cardConfiguration
                        .newBuilder()
                        .setSupportedCardTypes(*supportedCardTypesFromApi.orEmpty().toTypedArray())
                        .build()

                    dropInConfiguration.availableConfigs[PaymentMethodTypes.SCHEME] = newCardConfiguration
                }
            }
        }

        /**
         * Starts the checkout flow to be handled by the Drop-In solution. Make sure you have [DropInService] set up before calling this.
         * We suggest that you set up the resultHandlerIntent with the appropriate flags to clear the stack of the checkout activities.
         *
         * @param context A context to start the Checkout flow.
         * @param paymentMethodsApiResponse The result from the paymentMethods/ endpoint.
         * @param dropInConfiguration Additional required configuration data.
         * @param resultHandlerIntent The Intent used with [Activity.startActivity] that will contain the payment result extra with key [RESULT_KEY].
         *
         */
        @Deprecated("resultHandlerIntent need to pass with DropInConfiguration")
        fun startPayment(
            context: Context,
            paymentMethodsApiResponse: PaymentMethodsApiResponse,
            dropInConfiguration: DropInConfiguration,
            resultHandlerIntent: Intent
        ) {
            val newConfigurationBuilder = DropInConfiguration.Builder(dropInConfiguration)
            newConfigurationBuilder.setResultHandlerIntent(resultHandlerIntent)

            startPayment(context, paymentMethodsApiResponse, newConfigurationBuilder.build())
        }
    }
}
