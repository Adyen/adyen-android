/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DualBrandedCardUtilsTest {

    @Test
    fun testDualBrandSortingEmptyList() {
        val list = emptyList<DetectedCardType>()
        assertEquals(emptyList<DetectedCardType>(), DualBrandedCardUtils.sortBrands(list))
    }

    @Test
    fun testDualBrandSortingSingleItemList() {
        val detectedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )
        assertEquals(detectedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandVisaAndCarteBancaire() {
        val detectedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandVisaAndCarteBancaireAlreadySorted() {
        val detectedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandPlccAndMasterCard() {
        val detectedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandPlccAndMasterCardAlreadySorted() {
        val detectedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardBrand = CardBrand(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
            DetectedCardType(
                cardBrand = CardBrand(cardType = CardType.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
                paymentMethodVariant = null,
            ),
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }
}
