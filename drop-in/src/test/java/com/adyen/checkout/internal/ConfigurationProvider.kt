/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 28/10/2022.
 */

package com.adyen.checkout.internal

import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import java.util.Locale

internal object ConfigurationProvider {
    private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    private val TAG = LogUtil.getTag()
    private val shopperLocale = Locale.US
    private val amount = Amount(currency = "EUR", value = 1337)
    private val environment = Environment.TEST

    internal fun getDropInConfiguration(): DropInConfiguration {
        val dropInConfigurationBuilder = DropInConfiguration.Builder(
            shopperLocale,
            environment,
            TEST_CLIENT_KEY,
        )
            .addCardConfiguration(getCardConfiguration())
            .addBcmcConfiguration(getBcmcConfiguration())
            .addGooglePayConfiguration(getGooglePayConfiguration())
            .setEnableRemovingStoredPaymentMethods(true)

        try {
            dropInConfigurationBuilder.setAmount(amount)
        } catch (e: CheckoutException) {
            Logger.e(TAG, "Amount $amount not valid", e)
        }

        return dropInConfigurationBuilder.build()
    }

    private fun getCardConfiguration(): CardConfiguration =
        CardConfiguration.Builder(shopperLocale, environment, TEST_CLIENT_KEY).build()

    private fun getBcmcConfiguration(): BcmcConfiguration =
        BcmcConfiguration.Builder(shopperLocale, environment, TEST_CLIENT_KEY).build()

    private fun getGooglePayConfiguration(): GooglePayConfiguration =
        GooglePayConfiguration.Builder(shopperLocale, environment, TEST_CLIENT_KEY)
            .setCountryCode("NL")
            .setAmount(amount)
            .build()
}
