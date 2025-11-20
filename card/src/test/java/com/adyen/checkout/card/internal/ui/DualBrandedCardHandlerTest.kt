/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DualBrandedCardHandlerTest {

    private val dualBrandedCardHandler = DualBrandedCardHandler()

    @ParameterizedTest
    @MethodSource("dualBrandedCardHandlerSource")
    fun `given detected card types and selected brand, expected dual brand data should be created`(
        detectedCardTypes: List<DetectedCardType>,
        selectedBrand: CardBrand?,
        expectedDualBrandData: DualBrandData?
    ) {
        val actual = dualBrandedCardHandler.processDetectedCardTypes(detectedCardTypes, selectedBrand)
        assertEquals(expectedDualBrandData, actual)
    }

    companion object {

        @JvmStatic
        fun dualBrandedCardHandlerSource() = listOf(
            // detected card types, selected brand, expected dual brand data
            arguments(
                emptyList<DetectedCardType>(),
                null,
                null,
            ),
            arguments(
                listOf(
                    DetectedCardType(
                        CardBrand(CardType.VISA.txVariant),
                        isReliable = true,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "Visa",
                    ),
                    DetectedCardType(
                        CardBrand(CardType.CARTEBANCAIRE.txVariant),
                        isReliable = false,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "Cartes Bancaire",
                    ),
                ),
                null,
                null,
            ),
            arguments(
                listOf(
                    DetectedCardType(
                        CardBrand(CardType.VISA.txVariant),
                        isReliable = true,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "Visa",
                    ),
                    DetectedCardType(
                        CardBrand(CardType.CARTEBANCAIRE.txVariant),
                        isReliable = true,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "Cartes Bancaire",
                    ),
                ),
                null,
                DualBrandData(
                    brandOptionFirst = CardBrandItem(
                        name = "Visa",
                        brand = CardBrand(CardType.VISA.txVariant),
                        isSelected = true,
                    ),
                    brandOptionSecond = CardBrandItem(
                        name = "Cartes Bancaire",
                        brand = CardBrand(CardType.CARTEBANCAIRE.txVariant),
                        isSelected = false,
                    ),
                ),
            ),
            arguments(
                listOf(
                    DetectedCardType(
                        CardBrand(CardType.VISA.txVariant),
                        isReliable = true,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "Visa",
                    ),
                    DetectedCardType(
                        CardBrand(CardType.DANKORT.txVariant),
                        isReliable = true,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "Dankort",
                    ),
                ),
                CardBrand(CardType.DANKORT.txVariant),
                DualBrandData(
                    brandOptionFirst = CardBrandItem(
                        name = "Visa",
                        brand = CardBrand(CardType.VISA.txVariant),
                        isSelected = false,
                    ),
                    brandOptionSecond = CardBrandItem(
                        name = "Dankort",
                        brand = CardBrand(CardType.DANKORT.txVariant),
                        isSelected = true,
                    ),
                ),
            ),
            arguments(
                listOf(
                    DetectedCardType(
                        CardBrand(CardType.MASTERCARD.txVariant),
                        isReliable = true,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "Mastercard",
                    ),
                    DetectedCardType(
                        CardBrand("eft_pos"),
                        isReliable = true,
                        enableLuhnCheck = true,
                        cvcPolicy = Brand.FieldPolicy.REQUIRED,
                        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                        isSupported = true,
                        panLength = null,
                        paymentMethodVariant = null,
                        localizedBrand = "eftpos_australia",
                    ),
                ),
                null,
                null,
            ),
        )
    }
}
