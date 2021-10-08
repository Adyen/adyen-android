package com.adyen.checkout.card

import com.adyen.checkout.card.api.model.Brand
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import org.junit.Assert.assertEquals
import org.junit.Test

class DualBrandedCardUtilsTest {

    @Test
    fun testDualBrandSortingEmptyList() {
        val list = emptyList<DetectedCardType>()
        assertEquals(emptyList<DetectedCardType>(), DualBrandedCardUtils.sortBrands(list))
    }

    @Test
    fun testDualBrandSortingSingleItemList() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType.CARTEBANCAIRE,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )
        assertEquals(detectedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandVisaAndCarteBancaire() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType.CARTEBANCAIRE,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.VISA,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType.VISA,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.CARTEBANCAIRE,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandVisaAndCarteBancaireAlreadySorted() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType.VISA,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.CARTEBANCAIRE,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType.VISA,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.CARTEBANCAIRE,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandPlccAndMasterCard() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType.MASTERCARD,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.UNKNOWN.apply { txVariant = "plcc_mastercard" },
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType.UNKNOWN.apply { txVariant = "plcc_mastercard" },
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.MASTERCARD,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }

    @Test
    fun testDualBrandPlccAndMasterCardAlreadySorted() {
        val detectedCards = listOf(
            DetectedCardType(
                cardType = CardType.UNKNOWN.apply { txVariant = "plcc_mastercard" },
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.MASTERCARD,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        val sortedCards = listOf(
            DetectedCardType(
                cardType = CardType.UNKNOWN.apply { txVariant = "plcc_mastercard" },
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            ),
            DetectedCardType(
                cardType = CardType.MASTERCARD,
                isReliable = true,
                enableLuhnCheck = false,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true
            )
        )

        assertEquals(sortedCards, DualBrandedCardUtils.sortBrands(detectedCards))
    }
}
