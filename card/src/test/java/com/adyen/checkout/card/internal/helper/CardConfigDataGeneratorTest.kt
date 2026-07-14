/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2026.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.CheckoutParams
import com.adyen.checkout.core.components.internal.AnalyticsParams
import com.adyen.checkout.core.components.internal.AnalyticsParamsLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class CardConfigDataGeneratorTest {

    @ParameterizedTest
    @MethodSource("testSource")
    fun `when mapping, then expect`(
        configuration: CardComponentParams,
        isStored: Boolean,
        checkoutParams: CheckoutParams,
        expected: Map<String, String>
    ) {
        val cardConfigDataGenerator = CardConfigDataGenerator(checkoutParams)

        val result = cardConfigDataGenerator.generate(configuration, isStored)

        assertEquals(expected, result)
    }

    companion object {

        @JvmStatic
        fun testSource() = listOf(
            // All features enabled, not stored, ALWAYS_SHOW CVC
            arguments(
                createCardComponentParams(
                    showCardholderName = true,
                    supportedCardBrands = listOf(CardBrand("mc"), CardBrand("visa")),
                    showSupportedCardBrandLogos = true,
                    showStorePaymentMethod = true,
                    socialSecurityNumberVisibility = FieldVisibility.SHOW,
                    koreanAuthenticationVisibility = FieldVisibility.SHOW,
                    installmentParams = InstallmentParams(showInstallmentAmount = true),
                    showPostalCode = true,
                    cvcVisibility = CVCVisibility.ALWAYS_SHOW,
                    storedCVCVisibility = StoredCVCVisibility.SHOW,
                    showCardScanner = true,
                ),
                false,
                createCheckoutParams(showSubmitButton = true),
                mapOf(
                    "billingAddressMode" to "PostalCode",
                    "billingAddressRequired" to "true",
                    "brands" to "mc,visa",
                    "showSupportedCardBrandLogos" to "true",
                    "enableStoreDetails" to "true",
                    "hasHolderName" to "true",
                    "hasInstallmentOptions" to "true",
                    "hideCVC" to "show",
                    "holderNameRequired" to "true",
                    "showInstallmentAmounts" to "true",
                    "showKCPType" to "show",
                    "showPayButton" to "true",
                    "socialSecurityNumberMode" to "show",
                    "showCardScanner" to "true",
                ),
            ),
            // All features disabled, not stored, ALWAYS_HIDE CVC, installments with showAmount false
            arguments(
                createCardComponentParams(
                    showCardholderName = false,
                    supportedCardBrands = emptyList(),
                    showSupportedCardBrandLogos = false,
                    showStorePaymentMethod = false,
                    socialSecurityNumberVisibility = FieldVisibility.HIDE,
                    koreanAuthenticationVisibility = FieldVisibility.HIDE,
                    installmentParams = InstallmentParams(showInstallmentAmount = false),
                    showPostalCode = false,
                    cvcVisibility = CVCVisibility.ALWAYS_HIDE,
                    storedCVCVisibility = StoredCVCVisibility.HIDE,
                    showCardScanner = false,
                ),
                false,
                createCheckoutParams(showSubmitButton = false),
                mapOf(
                    "billingAddressMode" to "None",
                    "billingAddressRequired" to "false",
                    "brands" to "",
                    "showSupportedCardBrandLogos" to "false",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "true",
                    "hideCVC" to "hide",
                    "holderNameRequired" to "false",
                    "showInstallmentAmounts" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                    "showCardScanner" to "false",
                ),
            ),
            // No installments (showInstallmentAmounts key absent), not stored, HIDE_FIRST CVC
            arguments(
                createCardComponentParams(
                    showCardholderName = false,
                    supportedCardBrands = emptyList(),
                    showSupportedCardBrandLogos = false,
                    showStorePaymentMethod = false,
                    socialSecurityNumberVisibility = FieldVisibility.HIDE,
                    koreanAuthenticationVisibility = FieldVisibility.HIDE,
                    installmentParams = null,
                    showPostalCode = false,
                    cvcVisibility = CVCVisibility.HIDE_FIRST,
                    storedCVCVisibility = StoredCVCVisibility.SHOW,
                    showCardScanner = false,
                ),
                false,
                createCheckoutParams(showSubmitButton = false),
                mapOf(
                    "billingAddressMode" to "None",
                    "billingAddressRequired" to "false",
                    "brands" to "",
                    "showSupportedCardBrandLogos" to "false",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "false",
                    "hideCVC" to "auto",
                    "holderNameRequired" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                    "showCardScanner" to "false",
                ),
            ),
            // Stored card, StoredCVCVisibility.SHOW (cvcVisibility is ignored)
            arguments(
                createCardComponentParams(
                    showCardholderName = false,
                    supportedCardBrands = emptyList(),
                    showSupportedCardBrandLogos = false,
                    showStorePaymentMethod = false,
                    socialSecurityNumberVisibility = FieldVisibility.HIDE,
                    koreanAuthenticationVisibility = FieldVisibility.HIDE,
                    installmentParams = null,
                    showPostalCode = false,
                    cvcVisibility = CVCVisibility.ALWAYS_HIDE,
                    storedCVCVisibility = StoredCVCVisibility.SHOW,
                    showCardScanner = false,
                ),
                true,
                createCheckoutParams(showSubmitButton = false),
                mapOf(
                    "billingAddressMode" to "None",
                    "billingAddressRequired" to "false",
                    "brands" to "",
                    "showSupportedCardBrandLogos" to "false",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "false",
                    "hideCVC" to "show",
                    "holderNameRequired" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                    "showCardScanner" to "false",
                ),
            ),
            // Stored card, StoredCVCVisibility.HIDE (cvcVisibility is ignored)
            arguments(
                createCardComponentParams(
                    showCardholderName = false,
                    supportedCardBrands = emptyList(),
                    showSupportedCardBrandLogos = false,
                    showStorePaymentMethod = false,
                    socialSecurityNumberVisibility = FieldVisibility.HIDE,
                    koreanAuthenticationVisibility = FieldVisibility.HIDE,
                    installmentParams = null,
                    showPostalCode = false,
                    cvcVisibility = CVCVisibility.ALWAYS_SHOW,
                    storedCVCVisibility = StoredCVCVisibility.HIDE,
                    showCardScanner = false,
                ),
                true,
                createCheckoutParams(showSubmitButton = false),
                mapOf(
                    "billingAddressMode" to "None",
                    "billingAddressRequired" to "false",
                    "brands" to "",
                    "showSupportedCardBrandLogos" to "false",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "false",
                    "hideCVC" to "hide",
                    "holderNameRequired" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                    "showCardScanner" to "false",
                ),
            ),
        )

        @Suppress("LongParameterList")
        private fun createCardComponentParams(
            showCardholderName: Boolean,
            supportedCardBrands: List<CardBrand>,
            showSupportedCardBrandLogos: Boolean,
            showStorePaymentMethod: Boolean,
            socialSecurityNumberVisibility: FieldVisibility,
            koreanAuthenticationVisibility: FieldVisibility,
            installmentParams: InstallmentParams?,
            showPostalCode: Boolean,
            cvcVisibility: CVCVisibility,
            storedCVCVisibility: StoredCVCVisibility,
            showCardScanner: Boolean,
        ) = CardComponentParams(
            showCardholderName = showCardholderName,
            supportedCardBrands = supportedCardBrands,
            showSupportedCardBrandLogos = showSupportedCardBrandLogos,
            showStorePaymentMethod = showStorePaymentMethod,
            socialSecurityNumberVisibility = socialSecurityNumberVisibility,
            koreanAuthenticationVisibility = koreanAuthenticationVisibility,
            installmentParams = installmentParams,
            showPostalCode = showPostalCode,
            cvcVisibility = cvcVisibility,
            storedCVCVisibility = storedCVCVisibility,
            showCardScanner = showCardScanner,
        )

        private fun createCheckoutParams(
            showSubmitButton: Boolean,
        ) = CheckoutParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = "test_clientKey",
            analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL),
            amount = null,
            showSubmitButton = showSubmitButton,
            publicKey = null,
            additionalConfigurations = emptyMap(),
            additionalSessionParams = null,
        )
    }
}
