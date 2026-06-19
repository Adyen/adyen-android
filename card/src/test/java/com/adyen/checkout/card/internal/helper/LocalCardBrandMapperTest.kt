/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/6/2026.
 */

package com.adyen.checkout.card.internal.helper

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class LocalCardBrandMapperTest {

    @Test
    fun `when brand is supported and hideCvc is false then result has cvc REQUIRED and isSupported true`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertEquals(cardBrand, result.cardBrand)
        assertTrue(result.isSupported)
        assertEquals(Brand.FieldPolicy.REQUIRED, result.cvcPolicy)
    }

    @Test
    fun `when brand is not supported then isSupported is false`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = false, hideCvc = false)

        assertFalse(result.isSupported)
    }

    @Test
    fun `when hideCvc is true then cvcPolicy is HIDDEN`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = true)

        assertEquals(Brand.FieldPolicy.HIDDEN, result.cvcPolicy)
    }

    @Test
    fun `when brand is BCMC then cvcPolicy is HIDDEN regardless of hideCvc`() {
        val cardBrand = CardBrand(txVariant = CardType.BCMC.txVariant)

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertEquals(Brand.FieldPolicy.HIDDEN, result.cvcPolicy)
    }

    @Test
    fun `when brand is BCMC and hideCvc is true then cvcPolicy is HIDDEN`() {
        val cardBrand = CardBrand(txVariant = CardType.BCMC.txVariant)

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = true)

        assertEquals(Brand.FieldPolicy.HIDDEN, result.cvcPolicy)
    }

    @Test
    fun `when non-BCMC brand and hideCvc is false then cvcPolicy is REQUIRED`() {
        val cardBrand = CardBrand(txVariant = "amex")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertEquals(Brand.FieldPolicy.REQUIRED, result.cvcPolicy)
    }

    @Test
    fun `when mapping then enableLuhnCheck is always true`() {
        val cardBrand = CardBrand(txVariant = "mc")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertTrue(result.enableLuhnCheck)
    }

    @Test
    fun `when mapping then expiryDatePolicy is always REQUIRED`() {
        val cardBrand = CardBrand(txVariant = "mc")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertEquals(Brand.FieldPolicy.REQUIRED, result.expiryDatePolicy)
    }

    @Test
    fun `when mapping then isHidden is always false`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertFalse(result.isHidden)
    }

    @Test
    fun `when mapping then isShopperSelectionAllowedInDualBranded is always false`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertFalse(result.isShopperSelectionAllowedInDualBranded)
    }

    @Test
    fun `when mapping then panLength is always null`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertNull(result.panLength)
    }

    @Test
    fun `when mapping then paymentMethodVariant is always null`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertNull(result.paymentMethodVariant)
    }

    @Test
    fun `when mapping then localizedBrand is always null`() {
        val cardBrand = CardBrand(txVariant = "visa")

        val result = LocalCardBrandMapper.map(cardBrand = cardBrand, isSupported = true, hideCvc = false)

        assertNull(result.localizedBrand)
    }
}
