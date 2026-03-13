/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2026.
 */

package com.adyen.checkout.core.common.helper

/**
 * Possible validation results for social security number validation (@see
 * [SocialSecurityNumberValidator.validateSocialSecurityNumber]).
 */
sealed interface SocialSecurityNumberValidationResult {
    class Valid : SocialSecurityNumberValidationResult
    class Invalid : SocialSecurityNumberValidationResult
}
