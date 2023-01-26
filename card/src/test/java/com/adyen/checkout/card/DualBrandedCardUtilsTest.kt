package com.adyen.checkout.card

import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardBrand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.util.DualBrandedCardUtils
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
                cardType = CardType(cardBrand = CardBrand.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )
        assertEquals(detectedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandVisaAndCarteBancaire() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandVisaAndCarteBancaireAlreadySorted() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.VISA),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.CARTEBANCAIRE),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandPlccAndMasterCard() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandPlccAndMasterCardAlreadySorted() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType(txVariant = "plcc_mastercard"),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            ),
            DetectedCardType(
                cardType = CardType(cardBrand = CardBrand.MASTERCARD),
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = null,
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }
}
