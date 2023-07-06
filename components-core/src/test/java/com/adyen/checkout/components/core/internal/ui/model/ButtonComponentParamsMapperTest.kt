package com.adyen.checkout.components.core.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.ButtonTestConfiguration
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
        val componentConfiguration = getButtonConfigurationBuilder().build()

        val params = ButtonComponentParamsMapper(null, null).mapToParams(componentConfiguration, null)

        val expected = getButtonComponentParams()

        Assertions.assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override component configuration fields`() {
        val componentConfiguration = getButtonConfigurationBuilder().build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = GenericComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 49_00L
            )
        )

        val params = ButtonComponentParamsMapper(overrideParams, null).mapToParams(
            componentConfiguration,
            null
        )

        val expected = ButtonComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isAnalyticsEnabled = false,
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 49_00L
            ),
            isSubmitButtonVisible = true
        )

        Assertions.assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions if it exists, then should match drop in value, then configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val buttonConfiguration = getButtonConfigurationBuilder()
            .setAmount(configurationValue)
            .build()

        // this is in practice DropInComponentParams, but we don't have access to it in this module and any
        // ComponentParams class can work
        val overrideParams = dropInValue?.let { getButtonComponentParams().copy(amount = it) }

        val params = ButtonComponentParamsMapper(overrideParams, null).mapToParams(
            buttonConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentOptions = null,
                amount = sessionsValue,
                returnUrl = "",
            )
        )

        val expected = getButtonComponentParams().copy(amount = expectedValue)

        Assertions.assertEquals(expected, params)
    }

    private fun getButtonConfigurationBuilder(): ButtonTestConfiguration.Builder {
        return ButtonTestConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
    }

    private fun getButtonComponentParams(): ButtonComponentParams {
        return ButtonComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isAnalyticsEnabled = true,
            isCreatedByDropIn = false,
            amount = Amount.EMPTY,
            isSubmitButtonVisible = true,
        )
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )
    }
}
