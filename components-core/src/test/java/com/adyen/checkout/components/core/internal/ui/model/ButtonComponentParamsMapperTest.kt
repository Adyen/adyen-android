package com.adyen.checkout.components.core.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.ButtonTestConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class ButtonComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null then params should match the component configuration`() {
        val configuration = createCheckoutConfiguration()

        val params = ButtonComponentParamsMapper(false, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            sessionParams = null,
        )

        val expected = getButtonComponentParams()

        Assertions.assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override component configuration fields`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "EUR",
                value = 49_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            val testConfiguration = ButtonTestConfiguration.Builder(Locale.CANADA, Environment.TEST, TEST_CLIENT_KEY_1)
                .setAmount(Amount("USD", 1L))
                .setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
                .build()
            addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
        }

        val params = ButtonComponentParamsMapper(true, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            sessionParams = null,
        )

        val expected = ButtonComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 49_00L,
            ),
            isSubmitButtonVisible = true,
        )

        Assertions.assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions if it exists, then should match drop in value, then configuration`(
        configurationValue: Amount,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(configurationValue)

        val params = ButtonComponentParamsMapper(false, null).mapToParams(
            checkoutConfiguration = configuration,
            configuration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = sessionsValue,
                returnUrl = "",
            ),
        )

        val expected = getButtonComponentParams().copy(amount = expectedValue)

        Assertions.assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: ButtonTestConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        val testConfiguration = ButtonTestConfiguration.Builder(shopperLocale, environment, clientKey)
            .apply(configuration)
            .build()
        addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
    }

    private fun getButtonComponentParams(): ButtonComponentParams {
        return ButtonComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            isCreatedByDropIn = false,
            amount = null,
            isSubmitButtonVisible = true,
        )
    }

    companion object {
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), null, Amount("EUR", 100)),
        )
    }
}
