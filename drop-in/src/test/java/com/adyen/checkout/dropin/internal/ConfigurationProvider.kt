/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 28/10/2022.
 */

package com.adyen.checkout.dropin.internal

import com.adyen.checkout.bcmc.bcmcConfiguration
import com.adyen.checkout.card.cardConfiguration
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.dropInConfiguration
import com.adyen.checkout.googlepay.googlePayConfiguration
import java.util.Locale

internal object ConfigurationProvider {

    private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    private val shopperLocale = Locale.US
    private val amount = Amount(currency = "EUR", value = 1337)
    private val environment = Environment.TEST

    fun getCheckoutConfiguration() = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = TEST_CLIENT_KEY,
        amount = amount,
    ) {
        dropInConfiguration {
            setEnableRemovingStoredPaymentMethods(true)
        }

        cardConfiguration()

        bcmcConfiguration()

        googlePayConfiguration {
            setCountryCode("NL")
        }
    }
}
