/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/11/2022.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.AddressConfiguration
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.CardType
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
import com.adyen.checkout.components.core.internal.ui.model.DropInOverrideParams
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionInstallmentOptionsParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.core.Environment
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class CardComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom card configuration fields are null then all fields should match`() {
        val configuration = createCheckoutConfiguration()

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            null,
        )

        val expected = getCardComponentParams()

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom card configuration fields are set then all fields should match`() {
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

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            null,
        )

        val expected = getCardComponentParams(
            shopperLocale = Locale.FRANCE,
            environment = Environment.APSE,
            clientKey = TEST_CLIENT_KEY_2,
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
    fun `when parent configuration is set then parent configuration fields should override card configuration fields`() {
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

        val dropInOverrideParams = DropInOverrideParams(Amount("EUR", 123L))
        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), dropInOverrideParams, null)
            .mapToParamsDefault(
                configuration,
                PaymentMethod(),
                null,
            )

        val expected = getCardComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.NONE),
            isCreatedByDropIn = true,
            amount = Amount(
                currency = "EUR",
                value = 123L,
            ),
        )

        assertEquals(expected, params)
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

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            paymentMethod,
            null,
        )

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

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            paymentMethod,
            null,
        )

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

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            paymentMethod,
            null,
        )

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

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            null,
        )

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

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            sessionParams = SessionParams(
                enableStoreDetails = sessionsValue,
                installmentConfiguration = null,
                amount = null,
                returnUrl = "",
                shopperLocale = null,
            ),
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

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = null,
                amount = null,
                returnUrl = "",
                shopperLocale = null,
            ),
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

        val mapper = InstallmentsParamsMapper()

        val params = CardComponentParamsMapper(mapper, null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            sessionParams = SessionParams(
                enableStoreDetails = null,
                installmentConfiguration = installmentConfiguration,
                amount = null,
                returnUrl = "",
                shopperLocale = null,
            ),
        )

        val expected = getCardComponentParams(
            installmentParams = mapper.mapToInstallmentParams(
                installmentConfiguration = installmentConfiguration,
                amount = configuration.amount,
                shopperLocale = configuration.shopperLocale,
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

        val mapper = InstallmentsParamsMapper()

        val params = CardComponentParamsMapper(mapper, null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            sessionParams = null,
        )

        val expected = getCardComponentParams(
            installmentParams = mapper.mapToInstallmentParams(
                installmentConfiguration = installmentConfiguration,
                amount = configuration.amount,
                shopperLocale = configuration.shopperLocale,
            ),
        )

        assertEquals(expected, params)
    }

    @Test
    fun `installmentParams should be null if not set in configuration and there is no session`() {
        val configuration = createCheckoutConfiguration()

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            configuration,
            PaymentMethod(),
            sessionParams = null,
        )

        val expected = getCardComponentParams(
            installmentParams = null,
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
        val configuration = createCheckoutConfiguration(configurationValue)

        val dropInOverrideParams = dropInValue?.let { DropInOverrideParams(it) }

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), dropInOverrideParams, null)
            .mapToParamsDefault(
                configuration,
                PaymentMethod(),
                sessionParams = SessionParams(
                    enableStoreDetails = null,
                    installmentConfiguration = null,
                    amount = sessionsValue,
                    returnUrl = "",
                    shopperLocale = null,
                ),
            )

        val expected = getCardComponentParams(
            amount = expectedValue,
            isCreatedByDropIn = dropInOverrideParams != null,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when shopper locale is set in sessions then the mapped params should match it`() {
        val configuration = CheckoutConfiguration(
            environment = Environment.TEST,
            shopperLocale = Locale.US,
            clientKey = TEST_CLIENT_KEY_1,
        )

        val params = CardComponentParamsMapper(InstallmentsParamsMapper(), null, null).mapToParamsDefault(
            checkoutConfiguration = configuration,
            sessionParams = SessionParams(
                enableStoreDetails = false,
                installmentConfiguration = null,
                amount = null,
                returnUrl = null,
                shopperLocale = Locale.GERMAN,
            ),
            paymentMethod = PaymentMethod(),
        )

        assertEquals(Locale.GERMAN, params.shopperLocale)
    }

    private fun createCheckoutConfiguration(
        amount: Amount? = null,
        configuration: CardConfiguration.Builder.() -> Unit = {},
    ) = CheckoutConfiguration(
        shopperLocale = Locale.US,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY_1,
        amount = amount,
    ) {
        card(configuration)
    }

    @Suppress("LongParameterList")
    private fun getCardComponentParams(
        shopperLocale: Locale = Locale.US,
        environment: Environment = Environment.TEST,
        clientKey: String = TEST_CLIENT_KEY_1,
        analyticsParams: AnalyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
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
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        analyticsParams = analyticsParams,
        isCreatedByDropIn = isCreatedByDropIn,
        isHolderNameRequired = isHolderNameRequired,
        isSubmitButtonVisible = isSubmitButtonVisible,
        supportedCardBrands = supportedCardBrands,
        shopperReference = shopperReference,
        isStorePaymentFieldVisible = isStorePaymentFieldVisible,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        kcpAuthVisibility = kcpAuthVisibility,
        installmentParams = installmentParams,
        addressParams = addressParams,
        amount = amount,
        cvcVisibility = cvcVisibility,
        storedCVCVisibility = storedCVCVisibility,
    )

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"

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
    }
}
