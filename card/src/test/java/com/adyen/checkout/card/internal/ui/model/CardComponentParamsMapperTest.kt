/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/5/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.BillingAddressMode
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.InstallmentConfiguration
import com.adyen.checkout.card.InstallmentOptions
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.AdditionalSessionParams
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.data.model.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentConfiguration
import com.adyen.checkout.core.sessions.internal.model.SessionInstallmentOptionsParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class CardComponentParamsMapperTest {

    private val mapper = CardComponentParamsMapper()

    @Test
    fun `when card configuration is null then default values are used`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(cardConfiguration = null),
            paymentMethod = null,
        )

        val expected = createExpectedParams()
        assertEquals(expected, params)
    }

    @Test
    fun `when showCardholderName is null then it defaults to false`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = CardConfiguration(
                    showCardholderName = null,
                    supportedCardBrands = null,
                    showStorePaymentMethod = null,
                    showSecurityCode = null,
                    showSecurityCodeForStoredCard = null,
                    showSupportedCardBrandLogos = null,
                    socialSecurityNumberVisibility = null,
                    koreanAuthenticationVisibility = null,
                    billingAddressMode = null,
                    showCardScanner = null,
                    installmentConfiguration = null,
                ),
            ),
            paymentMethod = null,
        )

        assertEquals(false, params.showCardholderName)
    }

    @Test
    fun `when showCardholderName is true then it is passed through`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showCardholderName = true),
            ),
            paymentMethod = null,
        )

        assertEquals(true, params.showCardholderName)
    }

    @Test
    fun `when showStorePaymentMethod is null then it defaults to true`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showStorePaymentMethod = null),
            ),
            paymentMethod = null,
        )

        assertEquals(true, params.showStorePaymentMethod)
    }

    @Test
    fun `when showSecurityCode is null then cvcVisibility is ALWAYS_SHOW`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(cardConfiguration = createCardConfiguration(showSecurityCode = null)),
            paymentMethod = null,
        )

        assertEquals(CVCVisibility.ALWAYS_SHOW, params.cvcVisibility)
    }

    @Test
    fun `when showSecurityCode is true then cvcVisibility is ALWAYS_SHOW`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(cardConfiguration = createCardConfiguration(showSecurityCode = true)),
            paymentMethod = null,
        )

        assertEquals(CVCVisibility.ALWAYS_SHOW, params.cvcVisibility)
    }

    @Test
    fun `when showSecurityCode is false then cvcVisibility is ALWAYS_HIDE`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(cardConfiguration = createCardConfiguration(showSecurityCode = false)),
            paymentMethod = null,
        )

        assertEquals(CVCVisibility.ALWAYS_HIDE, params.cvcVisibility)
    }

    @Test
    fun `when showSecurityCodeForStoredCard is null then storedCVCVisibility is SHOW`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showSecurityCodeForStoredCard = null),
            ),
            paymentMethod = null,
        )

        assertEquals(StoredCVCVisibility.SHOW, params.storedCVCVisibility)
    }

    @Test
    fun `when showSecurityCodeForStoredCard is true then storedCVCVisibility is SHOW`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showSecurityCodeForStoredCard = true),
            ),
            paymentMethod = null,
        )

        assertEquals(StoredCVCVisibility.SHOW, params.storedCVCVisibility)
    }

    @Test
    fun `when showSecurityCodeForStoredCard is false then storedCVCVisibility is HIDE`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showSecurityCodeForStoredCard = false),
            ),
            paymentMethod = null,
        )

        assertEquals(StoredCVCVisibility.HIDE, params.storedCVCVisibility)
    }

    @Test
    fun `when showCardScanner is null then it defaults to true`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showCardScanner = null),
            ),
            paymentMethod = null,
        )

        assertTrue(params.showCardScanner)
    }

    @Test
    fun `when showCardScanner is true then it is passed through`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showCardScanner = true),
            ),
            paymentMethod = null,
        )

        assertTrue(params.showCardScanner)
    }

    @Test
    fun `when showCardScanner is false then it is passed through`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showCardScanner = false),
            ),
            paymentMethod = null,
        )

        assertFalse(params.showCardScanner)
    }

    @Test
    fun `when showSupportedCardBrandLogos is null then it defaults to true`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showSupportedCardBrandLogos = null),
            ),
            paymentMethod = null,
        )

        assertEquals(true, params.showSupportedCardBrandLogos)
    }

    @Test
    fun `when showSupportedCardBrandLogos is false then it is passed through`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showSupportedCardBrandLogos = false),
            ),
            paymentMethod = null,
        )

        assertEquals(false, params.showSupportedCardBrandLogos)
    }

    @Test
    fun `when socialSecurityNumberVisibility is null then it defaults to HIDE`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(socialSecurityNumberVisibility = null),
            ),
            paymentMethod = null,
        )

        assertEquals(FieldVisibility.HIDE, params.socialSecurityNumberVisibility)
    }

    @Test
    fun `when socialSecurityNumberVisibility is SHOW then it is passed through`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(socialSecurityNumberVisibility = FieldVisibility.SHOW),
            ),
            paymentMethod = null,
        )

        assertEquals(FieldVisibility.SHOW, params.socialSecurityNumberVisibility)
    }

    @Test
    fun `when koreanAuthenticationVisibility is null then it defaults to HIDE`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(koreanAuthenticationVisibility = null),
            ),
            paymentMethod = null,
        )

        assertEquals(FieldVisibility.HIDE, params.koreanAuthenticationVisibility)
    }

    @Test
    fun `when koreanAuthenticationVisibility is SHOW then it is passed through`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(koreanAuthenticationVisibility = FieldVisibility.SHOW),
            ),
            paymentMethod = null,
        )

        assertEquals(FieldVisibility.SHOW, params.koreanAuthenticationVisibility)
    }

    @Test
    fun `when supportedCardBrands is set in configuration then it takes priority over payment method brands`() {
        val configBrands = listOf(CardBrand(CardType.MAESTRO.txVariant))
        val paymentMethod = createCardPaymentMethod(
            brands = listOf(CardType.VISA.txVariant, CardType.MASTERCARD.txVariant),
        )

        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(supportedCardBrands = configBrands),
            ),
            paymentMethod = paymentMethod,
        )

        assertEquals(configBrands, params.supportedCardBrands)
    }

    @Test
    fun `when supportedCardBrands is null and payment method brands exist then payment method brands are used`() {
        val paymentMethod = createCardPaymentMethod(
            brands = listOf(CardType.VISA.txVariant, CardType.MASTERCARD.txVariant),
        )

        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(supportedCardBrands = null),
            ),
            paymentMethod = paymentMethod,
        )

        val expected = listOf(
            CardBrand(CardType.VISA.txVariant),
            CardBrand(CardType.MASTERCARD.txVariant),
        )
        assertEquals(expected, params.supportedCardBrands)
    }

    @Test
    fun `when supportedCardBrands and payment method brands are null then default list is used`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(supportedCardBrands = null),
            ),
            paymentMethod = null,
        )

        assertEquals(CardComponentParamsMapper.DEFAULT_SUPPORTED_CARDS_LIST, params.supportedCardBrands)
    }

    @ParameterizedTest
    @MethodSource("enableStoreDetailsSource")
    fun `showStorePaymentMethod should match sessions value if it exists, otherwise should match configuration`(
        configurationValue: Boolean?,
        sessionsValue: Boolean?,
        expectedValue: Boolean,
    ) {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(showStorePaymentMethod = configurationValue),
                additionalSessionParams = createAdditionalSessionParams(enableStoreDetails = sessionsValue),
            ),
            paymentMethod = null,
        )

        assertEquals(expectedValue, params.showStorePaymentMethod)
    }

    @Test
    fun `when installmentConfiguration is null then installmentParams is null`() {
        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(installmentConfiguration = null),
            ),
            paymentMethod = null,
        )

        assertEquals(null, params.installmentParams)
    }

    @Test
    fun `when installmentConfiguration has defaultOptions then installmentParams has defaultOptions`() {
        val installmentConfiguration = createInstallmentConfiguration(
            defaultOptions = createInstallmentOptions(values = listOf(2, 3, 6)),
            showInstallmentAmount = true,
        )

        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(installmentConfiguration = installmentConfiguration),
            ),
            paymentMethod = null,
        )

        assertEquals(listOf(2, 3, 6), params.installmentParams?.defaultOptions?.values)
        assertEquals(true, params.installmentParams?.showInstallmentAmount)
    }

    @Test
    fun `when installmentConfiguration has cardBasedOptions then installmentParams has cardBasedOptions`() {
        val mcBrand = CardBrand(CardType.MASTERCARD.txVariant)
        val installmentConfiguration = createInstallmentConfiguration(
            cardBasedOptions = mapOf(
                mcBrand to createInstallmentOptions(
                    values = listOf(3, 6, 9),
                    plans = listOf(InstallmentOptions.Plan.REGULAR, InstallmentOptions.Plan.REVOLVING),
                ),
            ),
        )

        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(installmentConfiguration = installmentConfiguration),
            ),
            paymentMethod = null,
        )

        assertEquals(listOf(3, 6, 9), params.installmentParams?.cardBasedOptions?.get(mcBrand)?.values)
    }

    @Test
    fun `when session has installmentConfiguration then it takes priority over merchant installmentConfiguration`() {
        val additionalSessionParams = createAdditionalSessionParams(
            installmentPlans = listOf("regular"),
            installmentValues = listOf(2, 3),
        )
        val merchantInstallmentConfiguration = createInstallmentConfiguration(
            defaultOptions = InstallmentOptions(
                maxInstallments = 12,
                plans = listOf(InstallmentOptions.Plan.REGULAR),
                preselectedValue = null,
            ),
        )

        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(
                    installmentConfiguration = merchantInstallmentConfiguration
                ),
                additionalSessionParams = additionalSessionParams,
            ),
            paymentMethod = null,
        )

        assertEquals(listOf(2, 3), params.installmentParams?.defaultOptions?.values)
    }

    @Test
    fun `when session has null installmentConfiguration then installmentParams is null regardless of merchant config`() {
        val additionalSessionParams = createAdditionalSessionParams()
        val merchantInstallmentConfiguration = createInstallmentConfiguration(
            defaultOptions = InstallmentOptions(
                maxInstallments = 6,
                plans = listOf(InstallmentOptions.Plan.REGULAR),
                preselectedValue = null,
            ),
        )

        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = createCardConfiguration(
                    installmentConfiguration = merchantInstallmentConfiguration
                ),
                additionalSessionParams = additionalSessionParams,
            ),
            paymentMethod = null,
        )

        assertEquals(null, params.installmentParams)
    }

    @Test
    fun `when all custom configuration fields are set then all fields should match`() {
        val customBrands = listOf(CardBrand(CardType.DINERS.txVariant), CardBrand(CardType.MAESTRO.txVariant))

        val params = mapper.mapToParams(
            params = generateCheckoutParams(
                cardConfiguration = CardConfiguration(
                    showCardholderName = true,
                    supportedCardBrands = customBrands,
                    showStorePaymentMethod = false,
                    showSecurityCode = false,
                    showSecurityCodeForStoredCard = false,
                    showSupportedCardBrandLogos = false,
                    socialSecurityNumberVisibility = FieldVisibility.SHOW,
                    koreanAuthenticationVisibility = FieldVisibility.SHOW,
                    billingAddressMode = BillingAddressMode.PostalCode(),
                    showCardScanner = false,
                    installmentConfiguration = createInstallmentConfiguration(
                        defaultOptions = createInstallmentOptions(values = listOf(2, 3, 6)),
                        showInstallmentAmount = true,
                    ),
                ),
            ),
            paymentMethod = null,
        )

        val expected = createExpectedParams(
            showCardholderName = true,
            supportedCardBrands = customBrands,
            showStorePaymentMethod = false,
            showSupportedCardBrandLogos = false,
            socialSecurityNumberVisibility = FieldVisibility.SHOW,
            koreanAuthenticationVisibility = FieldVisibility.SHOW,
            showPostalCode = true,
            cvcVisibility = CVCVisibility.ALWAYS_HIDE,
            storedCVCVisibility = StoredCVCVisibility.HIDE,
            showCardScanner = false,
            installmentParams = InstallmentParams(
                defaultOptions = InstallmentOptionsParams(
                    values = listOf(2, 3, 6),
                    plans = listOf(InstallmentPlan.REGULAR)
                ),
                showInstallmentAmount = true
            ),
        )

        assertEquals(expected, params)
    }

    private fun createAdditionalSessionParams(
        enableStoreDetails: Boolean? = null,
        installmentPlans: List<String> = emptyList(),
        installmentValues: List<Int> = emptyList(),
    ) = AdditionalSessionParams(
        enableStoreDetails = enableStoreDetails,
        showRemovePaymentMethodButton = null,
        returnUrl = "",
        installmentConfiguration = if (installmentPlans.isNotEmpty() || installmentValues.isNotEmpty()) {
            SessionInstallmentConfiguration(
                installmentOptions = mapOf(
                    "card" to SessionInstallmentOptionsParams(
                        plans = installmentPlans,
                        preselectedValue = null,
                        values = installmentValues,
                    ),
                ),
                showInstallmentAmount = null,
            )
        } else {
            null
        },
    )

    private fun generateCheckoutParams(
        cardConfiguration: CardConfiguration? = createCardConfiguration(),
        additionalSessionParams: AdditionalSessionParams? = null,
    ) = CheckoutParams(
        shopperLocale = DEVICE_LOCALE,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
        amount = null,
        showSubmitButton = true,
        publicKey = "test_publicKey",
        additionalConfigurations = buildMap {
            cardConfiguration?.let { this[CardConfiguration::class.java.name] = it }
        },
        additionalSessionParams = additionalSessionParams,
    )

    @Suppress("LongParameterList")
    private fun createCardConfiguration(
        showCardholderName: Boolean? = null,
        supportedCardBrands: List<CardBrand>? = null,
        showStorePaymentMethod: Boolean? = null,
        showSecurityCode: Boolean? = null,
        showSecurityCodeForStoredCard: Boolean? = null,
        showSupportedCardBrandLogos: Boolean? = null,
        socialSecurityNumberVisibility: FieldVisibility? = null,
        koreanAuthenticationVisibility: FieldVisibility? = null,
        billingAddressMode: BillingAddressMode? = null,
        showCardScanner: Boolean? = null,
        installmentConfiguration: InstallmentConfiguration? = null,
    ) = CardConfiguration(
        showCardholderName = showCardholderName,
        supportedCardBrands = supportedCardBrands,
        showStorePaymentMethod = showStorePaymentMethod,
        showSecurityCode = showSecurityCode,
        showSecurityCodeForStoredCard = showSecurityCodeForStoredCard,
        showSupportedCardBrandLogos = showSupportedCardBrandLogos,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        koreanAuthenticationVisibility = koreanAuthenticationVisibility,
        billingAddressMode = billingAddressMode,
        showCardScanner = showCardScanner,
        installmentConfiguration = installmentConfiguration,
    )

    private fun createCardPaymentMethod(
        brands: List<String> = emptyList(),
    ) = CardPaymentMethod(
        type = "scheme",
        name = "Card",
        brands = brands,
        fundingSource = null,
    )

    @Suppress("LongParameterList")
    private fun createExpectedParams(
        showCardholderName: Boolean = false,
        supportedCardBrands: List<CardBrand> = CardComponentParamsMapper.DEFAULT_SUPPORTED_CARDS_LIST,
        showStorePaymentMethod: Boolean = true,
        showSupportedCardBrandLogos: Boolean = true,
        socialSecurityNumberVisibility: FieldVisibility = FieldVisibility.HIDE,
        koreanAuthenticationVisibility: FieldVisibility = FieldVisibility.HIDE,
        showPostalCode: Boolean = false,
        cvcVisibility: CVCVisibility = CVCVisibility.ALWAYS_SHOW,
        storedCVCVisibility: StoredCVCVisibility = StoredCVCVisibility.SHOW,
        showCardScanner: Boolean = true,
        installmentParams: InstallmentParams? = null,
    ) = CardComponentParams(
        showCardholderName = showCardholderName,
        supportedCardBrands = supportedCardBrands,
        showStorePaymentMethod = showStorePaymentMethod,
        showSupportedCardBrandLogos = showSupportedCardBrandLogos,
        socialSecurityNumberVisibility = socialSecurityNumberVisibility,
        koreanAuthenticationVisibility = koreanAuthenticationVisibility,
        showPostalCode = showPostalCode,
        cvcVisibility = cvcVisibility,
        storedCVCVisibility = storedCVCVisibility,
        showCardScanner = showCardScanner,
        installmentParams = installmentParams,
    )

    private fun createInstallmentConfiguration(
        defaultOptions: InstallmentOptions? = null,
        cardBasedOptions: Map<CardBrand, InstallmentOptions> = emptyMap(),
        showInstallmentAmount: Boolean = false,
    ) = InstallmentConfiguration(
        defaultOptions = defaultOptions,
        cardBasedOptions = cardBasedOptions,
        showInstallmentAmount = showInstallmentAmount,
    )

    private fun createInstallmentOptions(
        values: List<Int>,
        plans: List<InstallmentOptions.Plan> = listOf(InstallmentOptions.Plan.REGULAR),
        preselectedValue: Int? = null,
    ) = InstallmentOptions(
        values = values,
        plans = plans,
        preselectedValue = preselectedValue,
    )

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private val DEVICE_LOCALE = Locale.forLanguageTag("nl-NL")

        @JvmStatic
        fun enableStoreDetailsSource() = listOf(
            // configurationValue, sessionsValue, expectedValue
            arguments(false, false, false),
            arguments(false, true, true),
            arguments(true, false, false),
            arguments(true, true, true),
            arguments(false, null, false),
            arguments(true, null, true),
            arguments(null, null, true),
            arguments(null, false, false),
            arguments(null, true, true),
        )
    }
}
