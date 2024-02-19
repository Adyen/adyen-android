package com.adyen.checkout.giftcard.internal.ui.model

import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
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

    private val giftCardComponentParamsMapper = GiftCardComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null then params should match the component configuration`() {
        val configuration = createCheckoutConfiguration {
            setPinRequired(false)
            setSubmitButtonVisible(false)
        }

        val params = giftCardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null)

        val expected = getComponentParams(
            isPinRequired = false,
            isSubmitButtonVisible = false,
        )

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
            giftCard {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)
        val params = giftCardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, dropInOverrideParams, null)

        val expected = getComponentParams(
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
    fun `amount should match value set in sessions then drop in then component configuration`(
        configurationValue: Amount,
        dropInValue: Amount?,
        sessionsValue: Amount?,
        expectedValue: Amount
    ) {
        val testConfiguration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }
        val sessionParams = SessionParams(
            enableStoreDetails = null,
            installmentConfiguration = null,
            amount = sessionsValue,
            returnUrl = "",
        )

        val params = giftCardComponentParamsMapper.mapToParams(
            checkoutConfiguration = testConfiguration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
        )

        val expected = getComponentParams(amount = expectedValue, isCreatedByDropIn = dropInOverrideParams != null)

        assertEquals(expected, params)
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

    @Suppress("LongParameterList")
    private fun getComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        isSubmitButtonVisible: Boolean = true,
        isPinRequired: Boolean = true,
    ): GiftCardComponentParams {
        return GiftCardComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsParams = analyticsParams,
                isCreatedByDropIn = isCreatedByDropIn,
                amount = amount,
            ),
            isSubmitButtonVisible = isSubmitButtonVisible,
            isPinRequired = isPinRequired,
        )
    }

    companion object {
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
    }
}
