package com.adyen.checkout.components.core.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.ButtonTestConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class ButtonComponentParamsMapperTest {

    private val buttonComponentParamsMapper = ButtonComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null then params should match the component configuration`() {
        val configuration = createCheckoutConfiguration()

        val params = buttonComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
        )

        val expected = getButtonComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override component configuration fields`() {
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

        val dropInOverrideParams = DropInOverrideParams(Amount("USD", 123L), null)
        val params = buttonComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
        )

        val expected = getButtonComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "USD",
                value = 123L,
            ),
            isSubmitButtonVisible = true,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when setSubmitButtonVisible is set to false in button component configuration and drop-in override params are set then card component params should have isSubmitButtonVisible true`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            val testConfiguration = ButtonTestConfiguration.Builder(Locale.CANADA, Environment.TEST, TEST_CLIENT_KEY_1)
                .setSubmitButtonVisible(false)
                .build()
            addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("USD", 123L), null)
        val params = buttonComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
        )

        assertEquals(true, params.isSubmitButtonVisible)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions then drop in then component configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val configuration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }
        val sessionParams = createSessionParams(
            amount = sessionsValue,
        )
        val params = buttonComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
        )

        val expected = getButtonComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("shopperLocaleSource")
    fun `shopper locale should match value set in configuration then sessions then device locale`(
        configurationValue: Locale?,
        sessionsValue: Locale?,
        deviceLocaleValue: Locale,
        expectedValue: Locale,
    ) {
        val configuration = createCheckoutConfiguration(shopperLocale = configurationValue)

        val sessionParams = createSessionParams(
            shopperLocale = sessionsValue,
        )

        val params = buttonComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
        )

        val expected = getButtonComponentParams(
            shopperLocale = expectedValue,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `environment and client key should match value set in sessions`() {
        val configuration = createCheckoutConfiguration()

        val sessionParams = createSessionParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        val params = buttonComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            componentConfiguration = configuration.getConfiguration(TEST_CONFIGURATION_KEY),
        )

        val expected = getButtonComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        configuration: ButtonTestConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        val testConfiguration = ButtonTestConfiguration.Builder(shopperLocale, environment, clientKey)
            .apply(configuration)
            .build()
        addConfiguration(TEST_CONFIGURATION_KEY, testConfiguration)
    }

    @Suppress("LongParameterList")
    private fun createSessionParams(
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        enableStoreDetails: Boolean? = null,
        installmentConfiguration: SessionInstallmentConfiguration? = null,
        showRemovePaymentMethodButton: Boolean? = null,
        amount: Amount? = null,
        returnUrl: String? = "",
        shopperLocale: Locale? = null,
    ) = SessionParams(
        environment = environment,
        clientKey = clientKey,
        enableStoreDetails = enableStoreDetails,
        installmentConfiguration = installmentConfiguration,
        showRemovePaymentMethodButton = showRemovePaymentMethodButton,
        amount = amount,
        returnUrl = returnUrl,
        shopperLocale = shopperLocale,
    )

    @Suppress("LongParameterList")
    private fun getButtonComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        isSubmitButtonVisible: Boolean = true,
    ): ButtonComponentParams {
        return ButtonComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsParams = analyticsParams,
                isCreatedByDropIn = isCreatedByDropIn,
                amount = amount,
            ),
            isSubmitButtonVisible = isSubmitButtonVisible,
        )
    }

    companion object {
        private const val TEST_CONFIGURATION_KEY = "TEST_CONFIGURATION_KEY"
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val DEVICE_LOCALE = Locale("nl", "NL")

        @JvmStatic
        fun amountSource() = listOf(
            // configurationValue, dropInValue, sessionsValue, expectedValue
            arguments(Amount("EUR", 100), Amount("USD", 200), Amount("CAD", 300), Amount("CAD", 300)),
            arguments(Amount("EUR", 100), Amount("USD", 200), null, Amount("USD", 200)),
            arguments(Amount("EUR", 100), null, null, Amount("EUR", 100)),
        )

        @JvmStatic
        fun shopperLocaleSource() = listOf(
            // configurationValue, sessionsValue, deviceLocaleValue, expectedValue
            arguments(null, null, Locale.US, Locale.US),
            arguments(Locale.GERMAN, null, Locale.US, Locale.GERMAN),
            arguments(null, Locale.CHINESE, Locale.US, Locale.CHINESE),
            arguments(Locale.GERMAN, Locale.CHINESE, Locale.US, Locale.GERMAN),
        )
    }
}
