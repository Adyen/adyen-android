/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.state.CardBrandData
import com.adyen.checkout.card.internal.ui.state.CardBrandState
import com.adyen.checkout.core.common.CardBrand
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

internal class DualBrandedCardHandlerTest {

    private val dualBrandedCardHandler = DualBrandedCardHandler()

    @Test
    fun `when card brand state is not DualBrandWithShopperSelection then result should be null`() {
        val cardBrandStates = listOf(
            CardBrandState.NoBrandsDetected,
            CardBrandState.UnsupportedBrand,
            CardBrandState.SingleReliableBrand(getCardBrandData()),
            CardBrandState.SingleUnreliableBrand(getCardBrandData()),
            CardBrandState.DualBrand(listOf(getCardBrandData(), getCardBrandData())),
        )

        cardBrandStates.forEach {
            val actual = dualBrandedCardHandler.getDualBrandData(it)
            assertNull(actual)
        }
    }

    @Test
    fun `when detected card types list is empty, then dual brand data is null`() {
        val cardBrandState = CardBrandState.DualBrandWithShopperSelection(
            cardBrandDataList = emptyList(),
            shopperSelectedCardBrandData = getCardBrandData(),
        )
        val actual = dualBrandedCardHandler.getDualBrandData(cardBrandState)
        assertNull(actual)
    }

    @Test
    fun `when detected card types list has a single item, then dual brand data is null`() {
        val cardBrandState = CardBrandState.DualBrandWithShopperSelection(
            cardBrandDataList = listOf(getCardBrandData()),
            shopperSelectedCardBrandData = getCardBrandData(),
        )
        val actual = dualBrandedCardHandler.getDualBrandData(cardBrandState)
        assertNull(actual)
    }

    @Test
    fun `when shopperSelectedCardBrandData is the first item, then brandOptionFirst should be selected`() {
        val firstCardBrandData = getCardBrandData().copy(cardBrand = CardBrand("first"))
        val secondCardBrandData = getCardBrandData().copy(cardBrand = CardBrand("second"))
        val cardBrandState = CardBrandState.DualBrandWithShopperSelection(
            cardBrandDataList = listOf(firstCardBrandData, secondCardBrandData),
            shopperSelectedCardBrandData = firstCardBrandData,
        )

        val actual = dualBrandedCardHandler.getDualBrandData(cardBrandState)

        assertNotNull(actual)
        assert(actual.brandOptionFirst.isSelected)
        assert(!actual.brandOptionSecond.isSelected)
    }

    @Test
    fun `when shopperSelectedCardBrandData is the second item, then brandOptionSecond should be selected`() {
        val firstCardBrandData = getCardBrandData().copy(cardBrand = CardBrand("first"))
        val secondCardBrandData = getCardBrandData().copy(cardBrand = CardBrand("second"))
        val cardBrandState = CardBrandState.DualBrandWithShopperSelection(
            cardBrandDataList = listOf(firstCardBrandData, secondCardBrandData),
            shopperSelectedCardBrandData = secondCardBrandData,
        )
        val actual = dualBrandedCardHandler.getDualBrandData(cardBrandState)

        assertNotNull(actual)
        assert(!actual.brandOptionFirst.isSelected)
        assert(actual.brandOptionSecond.isSelected)
    }

    @Test
    fun `when brand is available, then it should be mapped to CardBrandItem brand`() {
        val firstCardBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("first"),
        )
        val secondCardBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("second"),
        )
        val cardBrandState = CardBrandState.DualBrandWithShopperSelection(
            cardBrandDataList = listOf(firstCardBrandData, secondCardBrandData),
            shopperSelectedCardBrandData = firstCardBrandData,
        )
        val actual = dualBrandedCardHandler.getDualBrandData(cardBrandState)

        assertNotNull(actual)
        assertEquals(CardBrand("first"), actual.brandOptionFirst.brand)
        assertEquals(CardBrand("second"), actual.brandOptionSecond.brand)
    }

    @Test
    fun `when localizedBrand is available, then it should be mapped to CardBrandItem name`() {
        val firstCardBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("first"),
            localizedBrand = "localized first",
        )
        val secondCardBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("second"),
            localizedBrand = "localized second",
        )
        val cardBrandState = CardBrandState.DualBrandWithShopperSelection(
            cardBrandDataList = listOf(firstCardBrandData, secondCardBrandData),
            shopperSelectedCardBrandData = firstCardBrandData,
        )
        val actual = dualBrandedCardHandler.getDualBrandData(cardBrandState)

        assertNotNull(actual)
        assertEquals("localized first", actual.brandOptionFirst.name)
        assertEquals("localized second", actual.brandOptionSecond.name)
    }

    @Test
    fun `when localizedBrand is not available, then txVariant should be mapped to CardBrandItem name`() {
        val firstCardBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("first"),
        )
        val secondCardBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("second"),
        )
        val cardBrandState = CardBrandState.DualBrandWithShopperSelection(
            cardBrandDataList = listOf(firstCardBrandData, secondCardBrandData),
            shopperSelectedCardBrandData = firstCardBrandData,
        )
        val actual = dualBrandedCardHandler.getDualBrandData(cardBrandState)

        assertNotNull(actual)
        assertEquals("first", actual.brandOptionFirst.name)
        assertEquals("second", actual.brandOptionSecond.name)
    }

    private fun getCardBrandData(): CardBrandData {
        return CardBrandData(
            cardBrand = CardBrand(""),
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )
    }
}
