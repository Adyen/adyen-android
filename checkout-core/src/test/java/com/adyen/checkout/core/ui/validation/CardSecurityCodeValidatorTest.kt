/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */

package com.adyen.checkout.core.ui.validation

import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.CardType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CardSecurityCodeValidatorTest {

    @ParameterizedTest
    @MethodSource("securityCodeValidationSource")
    fun `when validateSecurityCode is called, then expected validation result is returned`(
        securityCodeInput: String,
        cardBrand: CardBrand? = null,
        expectedValidationResult: CardSecurityCodeValidationResult
    ) {
        val actualResult = CardSecurityCodeValidator.validateSecurityCode(securityCodeInput, cardBrand)
        assertEquals(expectedValidationResult, actualResult)
    }

    companion object {
        @JvmStatic
        fun securityCodeValidationSource() = listOf(
            arguments(
                "",
                CardBrand(CardType.VISA),
                CardSecurityCodeValidationResult.INVALID,
            ),
            arguments(
                "7",
                CardBrand(CardType.VISA),
                CardSecurityCodeValidationResult.INVALID,
            ),
            arguments(
                "12",
                CardBrand(CardType.VISA),
                CardSecurityCodeValidationResult.INVALID,
            ),
            arguments(
                "737",
                CardBrand(CardType.VISA),
                CardSecurityCodeValidationResult.VALID,
            ),
            arguments(
                "8689",
                CardBrand(CardType.VISA),
                CardSecurityCodeValidationResult.INVALID,
            ),
            arguments(
                "123456",
                CardBrand(CardType.VISA),
                CardSecurityCodeValidationResult.INVALID,
            ),
            arguments(
                "737",
                CardBrand(CardType.AMERICAN_EXPRESS),
                CardSecurityCodeValidationResult.INVALID,
            ),
            arguments(
                "8689",
                CardBrand(CardType.AMERICAN_EXPRESS),
                CardSecurityCodeValidationResult.VALID,
            ),
            arguments(
                "1%y",
                null,
                CardSecurityCodeValidationResult.INVALID,
            ),
        )
    }
}
