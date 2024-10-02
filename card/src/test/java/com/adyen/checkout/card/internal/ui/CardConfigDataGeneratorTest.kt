/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2024.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.KCPAuthVisibility
import com.adyen.checkout.card.SocialSecurityNumberVisibility
import com.adyen.checkout.card.internal.ui.model.AddressFieldPolicyParams
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParams
import com.adyen.checkout.components.core.internal.ui.model.AnalyticsParamsLevel
import com.adyen.checkout.components.core.internal.ui.model.CommonComponentParams
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.Environment
import com.adyen.checkout.ui.core.internal.ui.model.AddressParams
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

internal class CardConfigDataGeneratorTest {

    private lateinit var cardConfigDataGenerator: CardConfigDataGenerator

    @BeforeEach
    fun beforeEach() {
        cardConfigDataGenerator = CardConfigDataGenerator()
    }

    @ParameterizedTest
    @MethodSource("testSource")
    fun `when mapping, then expect`(
        configuration: CardComponentParams,
        isStored: Boolean,
        expected: Map<String, String>
    ) {
        val result = cardConfigDataGenerator.generate(configuration, isStored)

        assertEquals(expected, result)
    }

    companion object {

        @JvmStatic
        fun testSource() = listOf(
            arguments(
                createCardComponentParams(
                    isSubmitButtonVisible = true,
                    isHolderNameRequired = true,
                    supportedCardBrands = listOf(CardBrand("mc"), CardBrand("visa")),
                    isStorePaymentFieldVisible = true,
                    socialSecurityNumberVisibility = SocialSecurityNumberVisibility.SHOW,
                    kcpAuthVisibility = KCPAuthVisibility.SHOW,
                    installmentParams = InstallmentParams(shopperLocale = Locale.US, showInstallmentAmount = true),
                    addressParams = AddressParams.FullAddress(
                        supportedCountryCodes = listOf("US", "CA"),
                        addressFieldPolicy = AddressFieldPolicyParams.Required,
                    ),
                    cvcVisibility = CVCVisibility.ALWAYS_SHOW,
                    storedCVCVisibility = StoredCVCVisibility.HIDE,
                ),
                false,
                mapOf(
                    "billingAddressAllowedCountries" to "US,CA",
                    "billingAddressMode" to "full",
                    "billingAddressRequired" to "true",
                    "brands" to "mc,visa",
                    "enableStoreDetails" to "true",
                    "hasHolderName" to "true",
                    "hasInstallmentOptions" to "true",
                    "hideCVC" to "show",
                    "holderNameRequired" to "true",
                    "showInstallmentAmounts" to "true",
                    "showKCPType" to "show",
                    "showPayButton" to "true",
                    "socialSecurityNumberMode" to "show",
                ),
            ),
            arguments(
                createCardComponentParams(
                    isSubmitButtonVisible = false,
                    isHolderNameRequired = false,
                    supportedCardBrands = emptyList(),
                    isStorePaymentFieldVisible = false,
                    socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
                    kcpAuthVisibility = KCPAuthVisibility.HIDE,
                    installmentParams = InstallmentParams(shopperLocale = Locale.US, showInstallmentAmount = false),
                    addressParams = AddressParams.PostalCode(addressFieldPolicy = AddressFieldPolicyParams.Required),
                    cvcVisibility = CVCVisibility.HIDE_FIRST,
                    storedCVCVisibility = StoredCVCVisibility.SHOW,
                ),
                false,
                mapOf(
                    "billingAddressMode" to "partial",
                    "billingAddressRequired" to "true",
                    "brands" to "",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "true",
                    "hideCVC" to "auto",
                    "holderNameRequired" to "false",
                    "showInstallmentAmounts" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                ),
            ),
            arguments(
                createCardComponentParams(
                    isSubmitButtonVisible = false,
                    isHolderNameRequired = false,
                    supportedCardBrands = emptyList(),
                    isStorePaymentFieldVisible = false,
                    socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
                    kcpAuthVisibility = KCPAuthVisibility.HIDE,
                    installmentParams = null,
                    addressParams = AddressParams.Lookup(),
                    cvcVisibility = CVCVisibility.ALWAYS_HIDE,
                    storedCVCVisibility = StoredCVCVisibility.SHOW,
                ),
                false,
                mapOf(
                    "billingAddressMode" to "lookup",
                    "billingAddressRequired" to "true",
                    "brands" to "",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "false",
                    "hideCVC" to "hide",
                    "holderNameRequired" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                ),
            ),
            arguments(
                createCardComponentParams(
                    isSubmitButtonVisible = false,
                    isHolderNameRequired = false,
                    supportedCardBrands = emptyList(),
                    isStorePaymentFieldVisible = false,
                    socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
                    kcpAuthVisibility = KCPAuthVisibility.HIDE,
                    installmentParams = null,
                    addressParams = AddressParams.None,
                    cvcVisibility = CVCVisibility.ALWAYS_HIDE,
                    storedCVCVisibility = StoredCVCVisibility.SHOW,
                ),
                false,
                mapOf(
                    "billingAddressMode" to "none",
                    "billingAddressRequired" to "false",
                    "brands" to "",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "false",
                    "hideCVC" to "hide",
                    "holderNameRequired" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                ),
            ),
            arguments(
                createCardComponentParams(
                    isSubmitButtonVisible = false,
                    isHolderNameRequired = false,
                    supportedCardBrands = emptyList(),
                    isStorePaymentFieldVisible = false,
                    socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
                    kcpAuthVisibility = KCPAuthVisibility.HIDE,
                    installmentParams = null,
                    addressParams = AddressParams.None,
                    cvcVisibility = CVCVisibility.ALWAYS_HIDE,
                    storedCVCVisibility = StoredCVCVisibility.SHOW,
                ),
                true,
                mapOf(
                    "billingAddressMode" to "none",
                    "billingAddressRequired" to "false",
                    "brands" to "",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "false",
                    "hideCVC" to "show",
                    "holderNameRequired" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                ),
            ),
            arguments(
                createCardComponentParams(
                    isSubmitButtonVisible = false,
                    isHolderNameRequired = false,
                    supportedCardBrands = emptyList(),
                    isStorePaymentFieldVisible = false,
                    socialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE,
                    kcpAuthVisibility = KCPAuthVisibility.HIDE,
                    installmentParams = null,
                    addressParams = AddressParams.None,
                    cvcVisibility = CVCVisibility.ALWAYS_SHOW,
                    storedCVCVisibility = StoredCVCVisibility.HIDE,
                ),
                true,
                mapOf(
                    "billingAddressMode" to "none",
                    "billingAddressRequired" to "false",
                    "brands" to "",
                    "enableStoreDetails" to "false",
                    "hasHolderName" to "false",
                    "hasInstallmentOptions" to "false",
                    "hideCVC" to "hide",
                    "holderNameRequired" to "false",
                    "showKCPType" to "hide",
                    "showPayButton" to "false",
                    "socialSecurityNumberMode" to "hide",
                ),
            ),
        )

        @Suppress("LongParameterList")
        private fun createCardComponentParams(
            isSubmitButtonVisible: Boolean,
            isHolderNameRequired: Boolean,
            supportedCardBrands: List<CardBrand>,
            isStorePaymentFieldVisible: Boolean,
            socialSecurityNumberVisibility: SocialSecurityNumberVisibility,
            kcpAuthVisibility: KCPAuthVisibility,
            installmentParams: InstallmentParams?,
            addressParams: AddressParams,
            cvcVisibility: CVCVisibility,
            storedCVCVisibility: StoredCVCVisibility,
        ) = CardComponentParams(
            commonComponentParams = CommonComponentParams(
                shopperLocale = Locale.US,
                environment = Environment.TEST,
                clientKey = "clientKey",
                analyticsParams = AnalyticsParams(AnalyticsParamsLevel.ALL, ""),
                isCreatedByDropIn = false,
                amount = null,
            ),
            isSubmitButtonVisible = isSubmitButtonVisible,
            isHolderNameRequired = isHolderNameRequired,
            supportedCardBrands = supportedCardBrands,
            shopperReference = "shopperReference",
            isStorePaymentFieldVisible = isStorePaymentFieldVisible,
            socialSecurityNumberVisibility = socialSecurityNumberVisibility,
            kcpAuthVisibility = kcpAuthVisibility,
            installmentParams = installmentParams,
            addressParams = addressParams,
            cvcVisibility = cvcVisibility,
            storedCVCVisibility = storedCVCVisibility,
        )
    }
}
