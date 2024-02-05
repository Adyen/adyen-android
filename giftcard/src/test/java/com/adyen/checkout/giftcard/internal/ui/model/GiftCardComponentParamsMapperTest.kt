package com.adyen.checkout.giftcard.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.giftcard.GiftCardConfiguration
import com.adyen.checkout.giftcard.giftCard
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class GiftCardComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null then params should match the component configuration`() {
        val configuration = createCheckoutConfiguration {
            setPinRequired(false)
            setSubmitButtonVisible(false)
        }

        val params = GiftCardComponentParamsMapper(null, null).mapToParams(configuration, null)

        val expected = getComponentParams(
            isPinRequired = false,
            isSubmitButtonVisible = false,
        )

        assertEquals(expected, params)
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
            giftCard {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L))
        val params = GiftCardComponentParamsMapper(dropInOverrideParams, null).mapToParams(
            configuration,
            null,
        )

        val expected = GiftCardComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 123L,
            ),
            isPinRequired = true,
            isSubmitButtonVisible = true,
        )

        assertEquals(expected, params)
    }

    @ParameterizedTest
    @MethodSource("amountSource")
    fun `amount should match value set in sessions if it exists, then should match drop in value, then configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val testConfiguration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it) }

        val params = GiftCardComponentParamsMapper(dropInOverrideParams, null).mapToParams(
            testConfiguration,
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = sessionsValue,
                returnUrl = "",
                shopperLocale = null,
            ),
        )

        val expected = getComponentParams(amount = expectedValue, isCreatedByDropIn = dropInOverrideParams != null)

        assertEquals(expected, params)
    }

    @Test
    fun `when shopper locale is set in sessions then the mapped params should match it`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            shopperLocale = Locale.US,
            clientKey = TEST_CLIENT_KEY_1,
        )

        val params = GiftCardComponentParamsMapper(null, null).mapToParams(
            configuration = configuration,
            sessionParams = SessionParams(
                enableStoreDetails = false,
                installmentConfiguration = null,
                amount = null,
                returnUrl = null,
                shopperLocale = Locale.GERMAN,
            ),
        )

        assertEquals(Locale.GERMAN, params.shopperLocale)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: GiftCardConfiguration.Builder.() -> Unit = {}
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        giftCard(configuration)
    }

    private fun getComponentParams(
        amount: Amount? = null,
        isCreatedByDropIn: Boolean = false,
        isPinRequired: Boolean = true,
        isSubmitButtonVisible: Boolean = true,
    ): GiftCardComponentParams {
        return GiftCardComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
            isPinRequired = isPinRequired,
            isSubmitButtonVisible = isSubmitButtonVisible,
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
