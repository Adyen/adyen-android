/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/6/2026.
 */

package com.adyen.checkout.authentication.internal.ui.model

import com.adyen.checkout.authentication.AuthenticationConfiguration
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.components.internal.Configuration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Locale

internal class AuthenticationComponentParamsMapperTest {

    private val mapper = AuthenticationComponentParamsMapper()

    @Test
    fun `when authentication configuration is set, then params should match the configuration`() {
        val configuration = AuthenticationConfiguration(
            threeDSRequestorAppURL = TEST_APP_URL,
        )
        val params = mapper.mapToParams(checkoutParams(configuration))

        assertEquals(TEST_APP_URL, params.threeDSRequestorAppURL)
        assertEquals(AuthenticationComponentParamsMapper.DEVICE_PARAMETER_BLOCK_LIST, params.deviceParameterBlockList)
    }

    @Test
    fun `when no authentication configuration is set, then threeDSRequestorAppURL should be null`() {
        val params = mapper.mapToParams(checkoutParams(configuration = null))

        assertNull(params.threeDSRequestorAppURL)
        assertEquals(AuthenticationComponentParamsMapper.DEVICE_PARAMETER_BLOCK_LIST, params.deviceParameterBlockList)
    }

    private fun checkoutParams(configuration: AuthenticationConfiguration?) = CheckoutParams(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = null,
        additionalConfigurations = configuration?.let {
            mapOf<String, Configuration>(AuthenticationConfiguration::class.java.name to it)
        }.orEmpty(),
        additionalSessionParams = null,
    )

    companion object {
        private const val TEST_APP_URL = "https://adyen.com"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
    }
}
