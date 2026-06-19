/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/6/2026.
 */

package com.adyen.checkout.threeds2.internal.ui.model

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.internal.Configuration
import com.adyen.checkout.threeds2.ThreeDS2Configuration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Locale

internal class ThreeDS2ComponentParamsMapperTest {

    private val mapper = ThreeDS2ComponentParamsMapper()

    @Test
    fun `when 3ds2 configuration is set then params should match the configuration`() {
        val configuration = ThreeDS2Configuration(
            threeDSRequestorAppURL = TEST_APP_URL,
        )
        val params = mapper.mapToParams(checkoutParams(configuration))

        assertEquals(TEST_APP_URL, params.threeDSRequestorAppURL)
        assertEquals(ThreeDS2ComponentParamsMapper.DEVICE_PARAMETER_BLOCK_LIST, params.deviceParameterBlockList)
    }

    @Test
    fun `when no 3ds2 configuration is set then threeDSRequestorAppURL should be null`() {
        val params = mapper.mapToParams(checkoutParams(configuration = null))

        assertNull(params.threeDSRequestorAppURL)
        assertEquals(ThreeDS2ComponentParamsMapper.DEVICE_PARAMETER_BLOCK_LIST, params.deviceParameterBlockList)
    }

    private fun checkoutParams(configuration: ThreeDS2Configuration?) = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = null,
        additionalConfigurations = configuration?.let {
            mapOf<String, Configuration>(ThreeDS2Configuration::class.java.name to it)
        }.orEmpty(),
        additionalSessionParams = null,
    )

    companion object {
        private const val TEST_APP_URL = "https://adyen.com"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
    }
}
