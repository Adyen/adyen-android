/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.test.fake

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.example.ui.configuration.ConfigurationProvider
import com.adyen.checkout.test.server.CheckoutMockWebServer
import java.net.URL
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.full.primaryConstructor

@Singleton
internal class FakeCheckoutConfigurationProvider @Inject constructor() : ConfigurationProvider {

    private val environment = Environment::class.primaryConstructor!!.call(
        URL(CheckoutMockWebServer.baseUrl),
        URL(CheckoutMockWebServer.baseUrl),
    )

    var locale: Locale = Locale.US

    var amount: Amount? = null

    var configurationBlock: CheckoutConfiguration.() -> Unit = {}

    override val checkoutConfig: CheckoutConfiguration
        get() = CheckoutConfiguration(
            environment = environment,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = locale,
            amount = amount,
            configurationBlock = configurationBlock,
        )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
