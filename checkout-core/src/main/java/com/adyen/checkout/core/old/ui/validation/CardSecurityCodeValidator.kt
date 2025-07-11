/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/10/2024.
 */

package com.adyen.checkout.core.old.ui.validation

import com.adyen.checkout.core.old.CardBrand
import com.adyen.checkout.core.old.CardType
import com.adyen.checkout.core.old.internal.util.StringUtil

object CardSecurityCodeValidator {

    // Security Code
    private const val GENERAL_CARD_SECURITY_CODE_SIZE = 3
    private const val AMEX_SECURITY_CODE_SIZE = 4

    /**
     * Validate security code (CVV/CVC).
     *
     * @param securityCode Security code (CVV/CVC).
     * @param cardBrand Optional card brand parameter to apply specific validation to given security code
     * for a card brand.
     *
     * @return Validation result.
     */
    fun validateSecurityCode(securityCode: String, cardBrand: CardBrand? = null): CardSecurityCodeValidationResult {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)
        val length = normalizedSecurityCode.length
        return when {
            !StringUtil.isDigitsAndSeparatorsOnly(normalizedSecurityCode) -> CardSecurityCodeValidationResult.Invalid()
            cardBrand == CardBrand(cardType = CardType.AMERICAN_EXPRESS) &&
                length == AMEX_SECURITY_CODE_SIZE -> CardSecurityCodeValidationResult.Valid()

            cardBrand != CardBrand(cardType = CardType.AMERICAN_EXPRESS) &&
                length == GENERAL_CARD_SECURITY_CODE_SIZE -> CardSecurityCodeValidationResult.Valid()

            else -> CardSecurityCodeValidationResult.Invalid()
        }
    }
}
