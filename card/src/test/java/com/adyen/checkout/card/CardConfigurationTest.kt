/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.common.CardBrand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class CardConfigurationTest {

    @Test
    fun `when two configurations are created with the same values, then they are equal`() {
        val first = createConfiguration()
        val second = createConfiguration()

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `when two configurations are created with different values, then they are not equal`() {
        assertNotEquals(createConfiguration(), createConfiguration(billingAddressMode = BillingAddressMode.None()))
    }

    // Creates every nested value again on each call, so equality can't rely on shared instances.
    private fun createConfiguration(
        billingAddressMode: BillingAddressMode = BillingAddressMode.PostalCode(),
    ) = CardConfiguration(
        billingAddressMode = billingAddressMode,
        koreanAuthenticationVisibility = FieldVisibility.SHOW,
        showCardholderName = true,
        showSecurityCode = false,
        showSecurityCodeForStoredCard = true,
        showStorePaymentMethod = false,
        showSupportedCardBrandLogos = true,
        socialSecurityNumberVisibility = FieldVisibility.HIDE,
        supportedCardBrands = listOf(CardBrand("visa"), CardBrand("mc")),
        showCardScanner = false,
        installmentConfiguration = InstallmentConfiguration(
            defaultOptions = InstallmentOptions(
                values = listOf(2, 3),
                plans = listOf(InstallmentOptions.Plan.REGULAR),
                preselectedValue = 2,
            ),
            cardBasedOptions = mapOf(
                CardBrand("visa") to InstallmentOptions(
                    maxInstallments = 6,
                    plans = listOf(InstallmentOptions.Plan.REVOLVING),
                    preselectedValue = null,
                ),
            ),
            showInstallmentAmount = true,
        ),
    )
}
