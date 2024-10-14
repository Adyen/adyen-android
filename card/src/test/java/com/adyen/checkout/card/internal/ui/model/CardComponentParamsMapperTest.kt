/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/11/2022.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.card
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
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams
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

internal class CardComponentParamsMapperTest {

    private val cardComponentParamsMapper = CardComponentParamsMapper(
        CommonComponentParamsMapper(),
        InstallmentsParamsMapper(),
    )

    @Test
    fun `when drop-in override params are null and custom card configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = cardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, PaymentMethod())

        val expected = getCardComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are null and custom card configuration fields are set then all fields should match`() {
        val shopperReference = "SHOPPER_REFERENCE_1"
        val installmentConfiguration = InstallmentConfiguration(
            InstallmentOptions.DefaultInstallmentOptions(
                maxInstallments = 3,
                includeRevolving = true,
            ),
        )
        val expectedInstallmentParams = InstallmentParams(
            defaultOptions = InstallmentOptionParams.DefaultInstallmentOptions(
                values = listOf(2, 3),
                includeRevolving = true,
            ),
            shopperLocale = Locale.FRANCE,
        )

        val addressConfiguration = AddressConfiguration.FullAddress(supportedCountryCodes = listOf("CA", "GB"))
        val expectedAddressParams = AddressParams.FullAddress(
            supportedCountryCodes = addressConfiguration.supportedCountryCodes,
            addressFieldPolicy = AddressFieldPolicyParams.Required,
        )

        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            card {
                setHolderNameRequired(true)
                setSupportedCardTypes(CardType.DINERS, CardType.MAESTRO)
                setShopperReference(shopperReference)
                setShowStorePaymentField(false)
                setHideCvc(true)
                setHideCvcStoredCard(true)
                setSubmitButtonVisible(false)
                setSocialSecurityNumberVisibility(SocialSecurityNumberVisibility.SHOW)
                setKcpAuthVisibility(KCPAuthVisibility.SHOW)
                setInstallmentConfigurations(installmentConfiguration)
                setAddressConfiguration(addressConfiguration)
            }
        }

        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, TEST_CLIENT_KEY_2),
            isHolderNameRequired = true,
            supportedCardBrands = listOf(
                CardBrand(cardType = CardType.DINERS),
                CardBrand(cardType = CardType.MAESTRO),
            ),
            shopperReference = shopperReference,
            isStorePaymentFieldVisible = false,
            isSubmitButtonVisible = false,
            cvcVisibility = CVCVisibility.ALWAYS_HIDE,
            storedCVCVisibility = StoredCVCVisibility.HIDE,
            socialSecurityNumberVisibility = SocialSecurityNumberVisibility.SHOW,
            kcpAuthVisibility = KCPAuthVisibility.SHOW,
            installmentParams = expectedInstallmentParams,
            addressParams = expectedAddressParams,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when drop-in override params are set then they should override card configuration fields`() {
        val configuration = CheckoutConfiguration(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            amount = Amount(
                currency = "CAD",
                value = 1235_00L,
            ),
            analyticsConfiguration = AnalyticsConfiguration(AnalyticsLevel.NONE),
        ) {
            card {
                setAmount(Amount("USD", 1L))
                setAnalyticsConfiguration(AnalyticsConfiguration(AnalyticsLevel.ALL))
            }
        }

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L), null)
        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE, TEST_CLIENT_KEY_2),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 123L,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when setSubmitButtonVisible is set to false in card configuration and drop-in override params are set then card component params should have isSubmitButtonVisible true`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
        ) {
            card {
                setSubmitButtonVisible(false)
            }
        }
        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L), null)
        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        assertEquals(true, params.isSubmitButtonVisible)
    }

    @Test
    fun `when supported card types are set in the card configuration then they should be used in the params`() {
        val configuration = createCheckoutConfiguration {
            setSupportedCardTypes(CardType.MAESTRO, CardType.BCMC)
        }

        val paymentMethod = PaymentMethod(
            brands = listOf(
                CardType.VISA.txVariant,
                CardType.MASTERCARD.txVariant,
            ),
        )

        val params = cardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, paymentMethod)

        val expected = getCardComponentParams(
            supportedCardBrands = listOf(CardBrand(cardType = CardType.MAESTRO), CardBrand(cardType = CardType.BCMC)),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when there are any restricted card brand in payment method,they are removed from the params`() {
        val configuration = createCheckoutConfiguration()
        val paymentMethod = PaymentMethod(
            brands = listOf(
                RestrictedCardType.NYCE.txVariant,
                CardType.MASTERCARD.txVariant,
            ),
        )

        val params = cardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, paymentMethod)

        val expected = getCardComponentParams(
            supportedCardBrands = listOf(CardBrand(cardType = CardType.MASTERCARD)),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when supported card types are not set in the card configuration and payment method brands exist then brands should be used in the params`() {
        val configuration = createCheckoutConfiguration()

        val paymentMethod = PaymentMethod(
            brands = listOf(
                CardType.VISA.txVariant,
                CardType.MASTERCARD.txVariant,
            ),
        )

        val params = cardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, paymentMethod)

        val expected = getCardComponentParams(
            supportedCardBrands = listOf(
                CardBrand(cardType = CardType.VISA),
                CardBrand(cardType = CardType.MASTERCARD),
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when supported card types are not set in the card configuration and payment method brands do not exist then the default card types should be used in the params`() {
        val configuration = createCheckoutConfiguration()

        val params = cardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, PaymentMethod())

        val expected = getCardComponentParams(
            supportedCardBrands = CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST,
        )

        assertEquals(expected, params)
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

        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
            isStorePaymentFieldVisible = expectedValue,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `installmentParams should be null if set as null in sessions`() {
        val configuration = createCheckoutConfiguration {
            setInstallmentConfigurations(
                InstallmentConfiguration(
                    InstallmentOptions.DefaultInstallmentOptions(
                        maxInstallments = 3,
                        includeRevolving = true,
                    ),
                ),
            )
        }

        val sessionParams = createSessionParams()

        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
            installmentParams = null,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `installmentParams should match value set in sessions`() {
        val installmentOptions = mapOf(
            "card" to SessionInstallmentOptionsParams(
                plans = listOf("regular"),
                preselectedValue = 2,
                values = listOf(2),
            ),
        )
        val installmentConfiguration = SessionInstallmentConfiguration(
            installmentOptions = installmentOptions,
            showInstallmentAmount = false,
        )
        val configuration = createCheckoutConfiguration {
            setInstallmentConfigurations(
                InstallmentConfiguration(
                    defaultOptions = InstallmentOptions.DefaultInstallmentOptions(
                        maxInstallments = 3,
                        includeRevolving = true,
                    ),
                ),
            )
        }

        val installmentsParamsMapper = InstallmentsParamsMapper()
        val sessionParams = createSessionParams(
            installmentConfiguration = installmentConfiguration,
        )
        val cardComponentParamsMapper = CardComponentParamsMapper(
            CommonComponentParamsMapper(),
            installmentsParamsMapper,
        )

        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
            installmentParams = installmentsParamsMapper.mapToInstallmentParams(
                installmentConfiguration = installmentConfiguration,
                amount = configuration.amount,
                shopperLocale = configuration.shopperLocale ?: DEVICE_LOCALE,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `installmentParams should match configuration if there is no session`() {
        val installmentConfiguration = InstallmentConfiguration(
            InstallmentOptions.DefaultInstallmentOptions(
                maxInstallments = 3,
                includeRevolving = true,
            ),
        )
        val configuration = createCheckoutConfiguration {
            setInstallmentConfigurations(installmentConfiguration)
        }

        val installmentsParamsMapper = InstallmentsParamsMapper()
        val cardComponentParamsMapper = CardComponentParamsMapper(
            CommonComponentParamsMapper(),
            installmentsParamsMapper,
        )

        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = null,
            componentSessionParams = null,
            paymentMethod = PaymentMethod(),
        )

        val expected = getCardComponentParams(
            installmentParams = installmentsParamsMapper.mapToInstallmentParams(
                installmentConfiguration = installmentConfiguration,
                amount = configuration.amount,
                shopperLocale = configuration.shopperLocale ?: DEVICE_LOCALE,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `installmentParams should be null if not set in configuration and there is no session`() {
        val configuration = createCheckoutConfiguration()

        val params = cardComponentParamsMapper.mapToParams(configuration, DEVICE_LOCALE, null, null, PaymentMethod())

        val expected = getCardComponentParams(
            installmentParams = null,
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
        val configuration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it, null) }

        val sessionParams = createSessionParams(
            amount = sessionsValue,
        )

        val params = cardComponentParamsMapper.mapToParams(
            checkoutConfiguration = configuration,
            deviceLocale = DEVICE_LOCALE,
            dropInOverrideParams = dropInOverrideParams,
            componentSessionParams = sessionParams,
            paymentMethod = PaymentMethod(),
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

        val params = cardComponentParamsMapper.mapToParams(
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

        val params = cardComponentParamsMapper.mapToParams(
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
        configuration: CardConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        card(configuration)
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
        isHolderNameRequired: Boolean = false,
        isSubmitButtonVisible: Boolean = true,
        supportedCardBrands: List<CardBrand> = CardConfiguration.DEFAULT_SUPPORTED_CARDS_LIST,
        shopperReference: String? = null,
        isStorePaymentFieldVisible: Boolean = true,
        socialSecurityNumberVisibility: SocialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
        kcpAuthVisibility: KCPAuthVisibility = KCPAuthVisibility.HIDE,
        installmentParams: InstallmentParams? = null,
        addressParams: AddressParams = AddressParams.None,
        cvcVisibility: CVCVisibility = CVCVisibility.ALWAYS_SHOW,
        storedCVCVisibility: StoredCVCVisibility = StoredCVCVisibility.SHOW
    ) = CardComponentParams(
        commonComponentParams = CommonComponentParams(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
            analyticsParams = analyticsParams,
            isCreatedByDropIn = isCreatedByDropIn,
            amount = amount,
        ),
        isHolderNameRequired = isHolderNameRequired,
        isSubmitButtonVisible = isSubmitButtonVisible,
        supportedCardBrands = supportedCardBrands,
        shopperReference = shopperReference,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        kcpAuthVisibility = kcpAuthVisibility,
        installmentParams = installmentParams,
        addressParams = addressParams,
        cvcVisibility = cvcVisibility,
        storedCVCVisibility = storedCVCVisibility,
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
