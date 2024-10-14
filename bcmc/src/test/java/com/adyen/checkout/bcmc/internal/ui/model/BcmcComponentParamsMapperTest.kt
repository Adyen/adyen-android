/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

package com.adyen.checkout.bcmc.internal.ui.model

import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.bcmc.bcmc
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParamsMapper
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType
import com.adyen.checkout.core.Environment
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class BcmcComponentParamsMapperTest {

    private val bcmcComponentParamsMapper = BcmcComponentParamsMapper(CommonComponentParamsMapper())

    @Test
    fun `when drop-in override params are null and custom bcmc configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = bcmcComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, PaymentMethod())

        val expected = getCardComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom bcmc configuration fields are set then all fields should match`() {
        val shopperReference = "SHOPPER_REFERENCE_1"

        val configuration = createCheckoutConfiguration {
            setShopperReference(shopperReference)
            setHolderNameRequired(true)
            setShowStorePaymentField(true)
            setSubmitButtonVisible(false)
        }

        val params = bcmcComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, PaymentMethod())

        val expected = getCardComponentParams(
            isHolderNameRequired = true,
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = true,
            isSubmitButtonVisible = false,
            cvcVisibility = CVCVisibility.HIDE_FIRST,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override bcmc configuration fields`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "USD",
                value = 25_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            bcmc {
                setAmount(Amount("EUR", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)
        val params = bcmcComponentParamsMapper.mapToParams(
            configuration,
            DEVICE_LOCALE,
            dropInOverrideParams,
            null,
            PaymentMethod(),
        )

        val expected = getCardComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "CAD",
                value = 123L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when setSubmitButtonVisible is set to false in bcmc configuration and drop-in override params are set then card component params should have isSubmitButtonVisible true`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            bcmc {
                setSubmitButtonVisible(false)
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("CAD", 123L), null)
        val params = bcmcComponentParamsMapper.mapToParams(
            configuration,
            DEVICE_LOCALE,
            dropInOverrideParams,
            null,
            PaymentMethod(),
        )

        assertEquals(true, params.isSubmitButtonVisible)
    }

    @ParameterizedTest
    @MethodSource("enableStoreDetailsSource")
    @Suppress("MaxLineLength")
    fun `isStorePaymentFieldVisible should match value set in sessions if it exists, otherwise should match configuration`(
        configurationValue: Boolean,
        sessionsValue: Boolean?,
        expectedValue: Boolean
    ) {
        val configuration = createCheckoutConfiguration {
            setShowStorePaymentField(configurationValue)
        }

        val sessionParams = createSessionParams(
            enableStoreDetails = sessionsValue,
        )
        val params =
            bcmcComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, sessionParams, PaymentMethod())

        val expected = getCardComponentParams(isStorePaymentFieldVisible = expectedValue)

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
        val configuration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }

        val sessionParams = createSessionParams(
            amount = sessionsValue,
        )
        val params = bcmcComponentParamsMapper.mapToParams(
            configuration,
            DEVICE_LOCALE,
            dropInOverrideParams,
            sessionParams,
            PaymentMethod(),
        )

        val expected = getCardComponentParams(
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

        val params = bcmcComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = deviceLocaleValue,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
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

        val params = bcmcComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
            environment = Environment.INDIA,
            clientKey = TEST_CLIENT_KEY_2,
        )

        assertEquals(expected, params)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        shopperLocale: Locale? = null,
        configuration: BcmcConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        bcmc(configuration)
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
    private fun getCardComponentParams(
        shopperLocale: Locale = DEVICE_LOCALE,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_1),
        isCreatedByDropIn: Boolean = false,
        amount: Amount? = null,
        isSubmitButtonVisible: Boolean = true,
        isHolderNameRequired: Boolean = false,
        shopperReference: String? = null,
        isStorePaymentFieldVisible: Boolean = false,
        cvcVisibility: CVCVisibility = CVCVisibility.HIDE_FIRST,
    ) = CardComponentParams(
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
        ),
        isSubmitButtonVisible = isSubmitButtonVisible,
        isHolderNameRequired = isHolderNameRequired,
        shopperReference = shopperReference,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
        cvcVisibility = cvcVisibility,
        addressParams = AddressParams.None,
        installmentParams = null,
        socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
        kcpAuthVisibility = KCPAuthVisibility.HIDE,
        storedCVCVisibility = StoredCVCVisibility.HIDE,
        supportedCardBrands = listOf(
            CardBrand(cardType = CardType.BCMC),
            CardBrand(cardType = CardType.MAESTRO),
            CardBrand(cardType = CardType.VISA),
        ),
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
        private val DEVICE_LOCALE = Locale("nl", "NL")

        @JvmStatic
        fun enableStoreDetailsSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            arguments(false, false, false),
            arguments(false, true, true),
            arguments(true, false, false),
            arguments(true, true, true),
            arguments(false, null, false),
            arguments(true, null, true),
        )

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
