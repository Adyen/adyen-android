/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/10/2024.
 */

package com.adyen.checkout.core.common.helper

import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.common.internal.helper.StringUtil
import com.adyen.checkout.core.common.internal.properties.SecurityCodeProperties.SECURITY_CODE_MAX_LENGTH_AMEX
import com.adyen.checkout.core.common.internal.properties.SecurityCodeProperties.SECURITY_CODE_MAX_LENGTH_DEFAULT

/**
 * Validates card security codes (CVV/CVC).
 *
 * Use this when building a custom card UI to validate the security code independently of the
 * built-in card component.
 */
object CardSecurityCodeValidator {

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
            cardBrand == CardBrand(txVariant = CardType.AMERICAN_EXPRESS.txVariant) &&
                length == SECURITY_CODE_MAX_LENGTH_AMEX -> CardSecurityCodeValidationResult.Valid()

            cardBrand != CardBrand(txVariant = CardType.AMERICAN_EXPRESS.txVariant) &&
                length == SECURITY_CODE_MAX_LENGTH_DEFAULT -> CardSecurityCodeValidationResult.Valid()

            else -> CardSecurityCodeValidationResult.Invalid()
        }
    }
}
