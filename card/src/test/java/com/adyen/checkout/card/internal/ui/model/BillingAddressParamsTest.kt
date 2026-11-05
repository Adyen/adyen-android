/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/11/2026.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BillingAddressParamsTest {

    private val postalCode = BillingAddressParams.PostalCode(hideForCardBrands = setOf(CardBrand("cup"), CardBrand.JCB))
    private val full = BillingAddressParams.Full(
        supportedCountryCodes = emptySet(),
        hideForCardBrands = setOf(CardBrand("cup"), CardBrand.JCB),
    )

    @Test
    fun `when the mode is none, then no card brands are hidden`() {
        assertEquals(emptySet<CardBrand>(), BillingAddressParams.None.hideForCardBrands)
    }

    @Test
    fun `when the mode is none, then the postal code is hidden`() {
        assertEquals(RequirementPolicy.Hidden, BillingAddressParams.None.getPostalCodeRequirementPolicy())
    }

    @Test
    fun `when the mode is none, then the billing address row is hidden`() {
        assertFalse(BillingAddressParams.None.isBillingAddressVisible())
    }

    @Test
    fun `when the mode is postal code and no brand is detected, then the postal code is required`() {
        assertEquals(RequirementPolicy.Required, postalCode.getPostalCodeRequirementPolicy())
    }

    @Test
    fun `when the mode is postal code and a hidden brand is detected, then the postal code is hidden`() {
        assertEquals(RequirementPolicy.Hidden, postalCode.getPostalCodeRequirementPolicy(CardBrand.JCB))
    }

    @Test
    fun `when the mode is postal code and another brand is detected, then the postal code is required`() {
        assertEquals(RequirementPolicy.Required, postalCode.getPostalCodeRequirementPolicy(CardBrand.VISA))
    }

    @Test
    fun `when the mode is postal code, then the billing address row is hidden`() {
        assertFalse(postalCode.isBillingAddressVisible(CardBrand.VISA))
    }

    @Test
    fun `when the mode is full and no brand is detected, then the billing address row is visible`() {
        assertTrue(full.isBillingAddressVisible())
    }

    @Test
    fun `when the mode is full and a hidden brand is detected, then the billing address row is hidden`() {
        assertFalse(full.isBillingAddressVisible(CardBrand.JCB))
    }

    @Test
    fun `when the mode is full and another brand is detected, then the billing address row is visible`() {
        assertTrue(full.isBillingAddressVisible(CardBrand.VISA))
    }

    @Test
    fun `when the mode is full, then the postal code is hidden`() {
        assertEquals(RequirementPolicy.Hidden, full.getPostalCodeRequirementPolicy(CardBrand.VISA))
    }
}
