/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2026.
 */

package com.adyen.checkout.core.common.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.properties.SocialSecurityNumberProperties

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object SocialSecurityNumberValidator {

    fun validateSocialSecurityNumber(socialSecurityNumber: String): SocialSecurityNumberValidationResult {
        if (socialSecurityNumber.any { !it.isDigit() }) {
            return SocialSecurityNumberValidationResult.Invalid()
        }
        return when (socialSecurityNumber.length) {
            SocialSecurityNumberProperties.CPF_VALID_LENGTH -> SocialSecurityNumberValidationResult.Valid()
            SocialSecurityNumberProperties.CNPJ_VALID_LENGTH -> SocialSecurityNumberValidationResult.Valid()
            else -> SocialSecurityNumberValidationResult.Invalid()
        }
    }
}
