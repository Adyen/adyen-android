/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/4/2026.
 */

package com.adyen.checkout.card.internal.data.api

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class LocalCardBrandDetectionServiceTest {

    @Test
    fun `when getting card brands and the card number is empty, then an empty list should be returned`() {
        val cardNumber = ""

        val localCardBrandDetectionService = LocalCardBrandDetectionService(emptyList())
        val detectedCardTypes = localCardBrandDetectionService.getCardBrands(cardNumber)

        assert(detectedCardTypes.isEmpty())
    }

    @Test
    fun `when getting card brands then card brands are mapped correctly`() {
        val cardNumber = "5454"
        val cardBrand = CardBrand(CardType.MASTERCARD.txVariant)
        val supportedCardBrands = listOf(cardBrand)
        val localCardBrandDetectionService = LocalCardBrandDetectionService(supportedCardBrands)

        val detectedCardTypes = localCardBrandDetectionService.getCardBrands(cardNumber)
        val detectedCardType = detectedCardTypes.single { it.cardBrand == cardBrand }

        val expectedDetectedCardType = DetectedCardType(
            cardBrand = cardBrand,
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            isSupported = true,
            isShopperSelectionAllowedInDualBranded = false,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )

        assertEquals(expectedDetectedCardType, detectedCardType)
    }

    @Test
    fun `when getting card brands and the brand is not in supportedCardBrands, then returned detected card type should not be supported`() {
        val cardNumber = "5454"
        val cardBrand = CardBrand(CardType.MASTERCARD.txVariant)
        val localCardBrandDetectionService = LocalCardBrandDetectionService(emptyList())

        val detectedCardTypes = localCardBrandDetectionService.getCardBrands(cardNumber)
        val detectedCardType = detectedCardTypes.single { it.cardBrand == cardBrand }

        assertFalse(detectedCardType.isSupported)
    }

    @Test
    fun `when getting card brands and the brand is a no cvc brand, then returned detected card type should have cvc as hidden`() {
        val cardNumber = "6703"
        val cardBrand = CardBrand(CardType.BCMC.txVariant)
        val localCardBrandDetectionService = LocalCardBrandDetectionService(emptyList())

        val detectedCardTypes = localCardBrandDetectionService.getCardBrands(cardNumber)
        val detectedCardType = detectedCardTypes.single { it.cardBrand == cardBrand }

        assertEquals(Brand.FieldPolicy.HIDDEN, detectedCardType.cvcPolicy)
    }
}
