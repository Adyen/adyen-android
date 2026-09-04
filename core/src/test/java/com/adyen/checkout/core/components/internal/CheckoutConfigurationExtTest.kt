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
import com.adyen.checkout.core.components.data.model.Amount
import kotlinx.parcelize.Parcelize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

internal class CheckoutConfigurationExtTest {

    @Test
    fun `when copying then submit button visibility is replaced`() {
        val configuration = createConfiguration(showSubmitButton = false)

        val actual = configuration.copy(showSubmitButton = true)

        assertEquals(true, actual.showSubmitButton)
    }

    @Test
    fun `when copying without submit button visibility then it is not set`() {
        val configuration = createConfiguration(showSubmitButton = true)

        val actual = configuration.copy(showSubmitButton = null)

        assertNull(actual.showSubmitButton)
    }

    @Test
    fun `when copying then all other values are kept`() {
        val configuration = createConfiguration(
            environment = Environment.LIVE_UNITED_STATES,
            clientKey = "test_client_key",
            shopperLocale = Locale.FRANCE,
            amount = Amount("EUR", 1337),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
            showSubmitButton = false,
        )

        val actual = configuration.copy(showSubmitButton = true)

        assertEquals(Environment.LIVE_UNITED_STATES, actual.environment)
        assertEquals("test_client_key", actual.clientKey)
        assertEquals(Locale.FRANCE, actual.shopperLocale)
        assertEquals(Amount("EUR", 1337), actual.amount)
        assertEquals(AnalyticsLevel.NONE, actual.analyticsConfiguration?.level)
    }

    @Test
    fun `when copying then configurations added through the configuration block are kept`() {
        val paymentMethodConfiguration = TestConfiguration("test_value")
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        ) {
            addConfiguration(paymentMethodConfiguration)
        }

        val actual = configuration.copy(showSubmitButton = true)

        assertEquals(
            mapOf(TestConfiguration::class.java.name to paymentMethodConfiguration),
            actual.getAvailableConfigurations(),
        )
    }

    @Test
    fun `when copying then the original configuration is not modified`() {
        val configuration = createConfiguration(showSubmitButton = false)

        configuration.copy(showSubmitButton = true)

        assertEquals(false, configuration.showSubmitButton)
    }

    @Test
    fun `when copying then a new instance is returned`() {
        val configuration = createConfiguration()

        val actual = configuration.copy(showSubmitButton = true)

        assertTrue(configuration !== actual)
    }

    /**
     * [copy] has to list every value of [CheckoutConfiguration] to carry it over. A value that is added to the class
     * but not to [copy] is silently dropped for every caller of [copy], which is why this test fails when the primary
     * constructor changes.
     */
    @Test
    fun `when CheckoutConfiguration gains a value then copy has to be updated`() {
        val expected = listOf(
            Environment::class.java,
            String::class.java,
            Locale::class.java,
            Amount::class.java,
            AnalyticsConfiguration::class.java,
            Boolean::class.javaObjectType,
            Function1::class.java,
        )

        val actual = CheckoutConfiguration::class.java.declaredConstructors
            .filterNot { constructor ->
                constructor.parameterTypes.any { it.name.endsWith("DefaultConstructorMarker") }
            }
            .maxBy { it.parameterCount }
            .parameterTypes
            .toList()

        assertEquals(
            expected,
            actual,
            "The primary constructor of CheckoutConfiguration changed. Update CheckoutConfiguration.copy() to " +
                "carry the new value over, then update this test.",
        )
    }

    @Suppress("LongParameterList")
    private fun createConfiguration(
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY,
        shopperLocale: Locale? = null,
        amount: Amount? = null,
        analyticsConfiguration: AnalyticsConfiguration? = null,
        showSubmitButton: Boolean? = null,
    ) = CheckoutConfiguration(
        environment = environment,
        clientKey = clientKey,
        shopperLocale = shopperLocale,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
        showSubmitButton = showSubmitButton,
    )

    @Parcelize
    private data class TestConfiguration(val value: String) : Configuration

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfgh"
    }
}
