/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal

import com.adyen.checkout.bcmc.bcmc
import com.adyen.checkout.card.old.card
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.old.Environment
import com.adyen.checkout.dropin.old.dropIn
import com.adyen.checkout.googlepay.old.googlePay
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
        dropIn {
            setEnableRemovingStoredPaymentMethods(true)
        }

        card()

        bcmc()

        googlePay {
            setCountryCode("NL")
        }
    }
}
