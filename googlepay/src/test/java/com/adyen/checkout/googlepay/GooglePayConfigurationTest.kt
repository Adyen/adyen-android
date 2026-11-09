/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.googlepay

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class GooglePayConfigurationTest {

    @Test
    fun `when two configurations are created with the same values, then they are equal`() {
        val first = createConfiguration()
        val second = createConfiguration()

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `when two configurations are created with different values, then they are not equal`() {
        assertNotEquals(createConfiguration(), createConfiguration(countryCode = "US"))
    }

    // Creates every nested value again on each call, so equality can't rely on shared instances.
    private fun createConfiguration(
        countryCode: String = "NL",
    ) = GooglePayConfiguration(
        merchantAccount = "merchant_account",
        googlePayEnvironment = 3,
        totalPriceStatus = "ESTIMATED",
        countryCode = countryCode,
        merchantInfo = MerchantInfo(
            merchantName = "Merchant",
            softwareInfo = SoftwareInfo(id = "software_id", version = "1.0.0"),
        ),
        allowedPaymentMethods = GooglePayAllowedPaymentMethods(
            card = GooglePayAllowedPaymentMethods.Card(
                allowedAuthMethods = listOf("PAN_ONLY"),
                allowedCardNetworks = listOf("VISA"),
                allowPrepaidCards = false,
                allowCreditCards = true,
                issuerCountryCodes = IssuerCountryCodes.Allowed(listOf("NL")),
                assuranceDetailsRequired = true,
                billingAddressRequired = true,
                billingAddressParameters = BillingAddressParameters(format = "FULL", isPhoneNumberRequired = true),
            ),
        ),
        isEmailRequired = true,
        isExistingPaymentMethodRequired = false,
        isShippingAddressRequired = true,
        shippingAddressParameters = ShippingAddressParameters(
            allowedCountryCodes = listOf("NL", "BE"),
            isPhoneNumberRequired = true,
        ),
        checkoutOption = "COMPLETE_IMMEDIATE_PURCHASE",
        appearance = GooglePayButtonAppearance(
            theme = GooglePayButtonTheme.DARK,
            type = GooglePayButtonType.PAY,
            cornerRadius = 8,
        ),
    )
}
