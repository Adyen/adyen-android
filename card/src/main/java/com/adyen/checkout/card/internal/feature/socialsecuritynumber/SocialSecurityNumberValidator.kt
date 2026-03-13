/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.card.internal.feature.socialsecuritynumber

import com.adyen.checkout.card.internal.feature.socialsecuritynumber.SocialSecurityNumberProperties.CNPJ_VALID_LENGTH
import com.adyen.checkout.card.internal.feature.socialsecuritynumber.SocialSecurityNumberProperties.CPF_VALID_LENGTH

internal object SocialSecurityNumberValidator {

    fun validateSocialSecurityNumber(socialSecurityNumber: String): Boolean {
        val digitLength = socialSecurityNumber.filter { it.isDigit() }.length
        return when (digitLength) {
            CPF_VALID_LENGTH -> true
            CNPJ_VALID_LENGTH -> true
            else -> false
        }
    }
}
