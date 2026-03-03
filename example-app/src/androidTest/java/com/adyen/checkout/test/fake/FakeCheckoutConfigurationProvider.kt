/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.test.fake

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.example.ui.configuration.ConfigurationProvider
import com.adyen.checkout.test.server.CheckoutMockWebServer
import java.net.URL
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.full.primaryConstructor
import com.adyen.checkout.components.core.CheckoutConfiguration as OldCheckoutConfiguration
import com.adyen.checkout.core.old.Environment as OlEnvironment

@Singleton
internal class FakeCheckoutConfigurationProvider @Inject constructor() : ConfigurationProvider {

    private val environment = OlEnvironment::class.primaryConstructor!!.call(
        URL(CheckoutMockWebServer.baseUrl),
        URL(CheckoutMockWebServer.baseUrl),
    )

    var locale: Locale = Locale.US

    var amount: Amount? = null

    var configurationBlock: OldCheckoutConfiguration.() -> Unit = {}

    override val oldCheckoutConfig: OldCheckoutConfiguration
        get() = OldCheckoutConfiguration(
            environment = environment,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = locale,
            amount = amount,
            configurationBlock = configurationBlock,
        )

    override val checkoutConfig: CheckoutConfiguration
        get() = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
            shopperLocale = locale,
            amount = null,
        )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
