/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/3/2023.
 */

package com.adyen.checkout.adyen3ds2.internal.ui.model

import com.adyen.checkout.adyen3ds2.adyen3DS2
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.core.Environment
import com.adyen.threeds2.customization.UiCustomization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class Adyen3DS2ComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom 3ds2 configuration fields are null then all fields should match`() {
        val checkoutConfiguration = getCheckoutConfiguration()

        val params = Adyen3DS2ComponentParamsMapper(null, null)
            .mapToParams(checkoutConfiguration, null)

        val expected = getAdyen3DS2ComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom 3ds2 configuration fields are set then all fields should match`() {
        val uiCustomization = UiCustomization()

        val testUrl = "https://adyen.com"
        val configuration = getCheckoutConfiguration {
            adyen3DS2 {
                setUiCustomization(uiCustomization)
                setThreeDSRequestorAppURL(testUrl)
            }
        }

        val params = Adyen3DS2ComponentParamsMapper(null, null)
            .mapToParams(configuration, null)

        val expected = getAdyen3DS2ComponentParams(
            uiCustomization = uiCustomization,
            threeDSRequestorAppURL = testUrl,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override 3ds2 configuration fields`() {
        val checkoutConfiguration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
            amount = Amount(
                currency = "USD",
                value = 25_00L,
            ),
        )

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L))
        val params = Adyen3DS2ComponentParamsMapper(dropInOverrideParams, null)
            .mapToParams(checkoutConfiguration, null)

        val expected = getAdyen3DS2ComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 123L,
            ),
        )

        assertEquals(expected, params)
    }

    private fun getCheckoutConfiguration(config: CheckoutConfiguration.() -> Unit = {}) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        configurationBlock = config,
    )

    @Suppress("LongParameterList")
    private fun getAdyen3DS2ComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        uiCustomization: UiCustomization? = null,
        threeDSRequestorAppURL: String? = null,
    ) = Adyen3DS2ComponentParams(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        analyticsParams = analyticsParams,
        isCreatedByDropIn = isCreatedByDropIn,
        amount = amount,
        uiCustomization = uiCustomization,
        threeDSRequestorAppURL = threeDSRequestorAppURL,
        deviceParameterBlockList = Adyen3DS2ComponentParamsMapper.DEVICE_PARAMETER_BLOCK_LIST,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
    }
}
