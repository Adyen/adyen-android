/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/9/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.AnalyticsConfiguration
import com.adyen.checkout.core.components.AnalyticsLevel
import com.adyen.checkout.core.components.CheckoutConfiguration
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CheckoutConfigurationExtTest {

    @Test
    fun `when submit button visibility is replaced then all other values are kept`() {
        val configuration = createConfiguration(showSubmitButton = false)

        val actual = configuration.withShowSubmitButton(true)

        assertEquals(createConfiguration(showSubmitButton = true), actual)
    }

    @Test
    fun `when submit button visibility is replaced with null then it is not set`() {
        val configuration = createConfiguration(showSubmitButton = true)

        val actual = configuration.withShowSubmitButton(null)

        assertEquals(createConfiguration(showSubmitButton = null), actual)
    }

    @Test
    fun `when submit button visibility is replaced then the original configuration is not modified`() {
        val configuration = createConfiguration(showSubmitButton = false)

        configuration.withShowSubmitButton(true)

        assertEquals(createConfiguration(showSubmitButton = false), configuration)
    }

    @Test
    fun `when submit button visibility is replaced then a new instance is returned`() {
        val configuration = createConfiguration(showSubmitButton = false)

        val actual = configuration.withShowSubmitButton(false)

        assertTrue(configuration !== actual)
    }

    // Creates every value again on each call, so equality can't rely on shared instances.
    private fun createConfiguration(showSubmitButton: Boolean?) = CheckoutConfiguration(
        environment = Environment.LIVE_UNITED_STATES,
        clientKey = TEST_CLIENT_KEY,
        shopperLocale = Locale.FRANCE,
        analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        showSubmitButton = showSubmitButton,
    ) {
        addConfiguration(TestConfiguration("test_value"))
    }

    @Parcelize
    private data class TestConfiguration(val value: String) : Configuration

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfgh"
    }
}
