/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/7/2025.
 */

package com.adyen.checkout.card.internal.util

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType
import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DualBrandedCardHandlerTest {

    private val dualBrandedCardHandler = DualBrandedCardHandler(Environment.TEST)

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
                null
            ),
            arguments(
                listOf(
                    DetectedCardType(
                        CardBrand(CardType.VISA),
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
                        CardBrand(CardType.CARTEBANCAIRE),
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
                        CardBrand(CardType.VISA),
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
                        CardBrand(CardType.CARTEBANCAIRE),
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
                    brandOptions = listOf(
                        CardBrandItem(
                            name = "Visa",
                            brand = CardBrand(CardType.VISA),
                            isSelected = true,
                            environment = Environment.TEST,
                        ),
                        CardBrandItem(
                            name = "Cartes Bancaire",
                            brand = CardBrand(CardType.CARTEBANCAIRE),
                            isSelected = false,
                            environment = Environment.TEST,
                        ),
                    ),
                    selectedBrand = CardBrand(CardType.VISA),
                    selectable = true,
                ),
            ),
            arguments(
                listOf(
                    DetectedCardType(
                        CardBrand(CardType.VISA),
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
                        CardBrand(CardType.DANKORT),
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
                CardBrand(CardType.DANKORT),
                DualBrandData(
                    brandOptions = listOf(
                        CardBrandItem(
                            name = "Visa",
                            brand = CardBrand(CardType.VISA),
                            isSelected = false,
                            environment = Environment.TEST,
                        ),
                        CardBrandItem(
                            name = "Dankort",
                            brand = CardBrand(CardType.DANKORT),
                            isSelected = true,
                            environment = Environment.TEST,
                        ),
                    ),
                    selectedBrand = CardBrand(CardType.DANKORT),
                    selectable = true,
                ),
            ),
            arguments(
                listOf(
                    DetectedCardType(
                        CardBrand(CardType.MASTERCARD),
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
                DualBrandData(
                    brandOptions = listOf(
                        CardBrandItem(
                            name = "Mastercard",
                            brand = CardBrand(CardType.MASTERCARD),
                            isSelected = false,
                            environment = Environment.TEST,
                        ),
                        CardBrandItem(
                            name = "eftpos_australia",
                            brand = CardBrand("eft_pos"),
                            isSelected = false,
                            environment = Environment.TEST,
                        ),
                    ),
                    selectedBrand = null,
                    selectable = false,
                ),
            )
        )
    }
}
