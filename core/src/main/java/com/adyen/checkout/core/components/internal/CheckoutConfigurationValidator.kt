/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.internal.helper.ClientKeyValidationResult
import com.adyen.checkout.core.common.internal.helper.ClientKeyValidator
import com.adyen.checkout.core.common.internal.helper.LocaleUtil
import com.adyen.checkout.core.common.internal.helper.LocaleValidationResult
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.error.CheckoutError

/**
 * Validates the [CheckoutConfiguration] and returns a [CheckoutError] if validation fails.
 *
 * @return [CheckoutError] if validation fails, `null` otherwise.
 */
@Suppress("ReturnCount")
internal fun CheckoutConfiguration.validate(): CheckoutError? {
    when (val result = ClientKeyValidator.validateClientKey(clientKey)) {
        is ClientKeyValidationResult.Valid -> { /* valid */ }
        is ClientKeyValidationResult.Invalid -> return result.error
    }

    shopperLocale?.let { locale ->
        when (val result = LocaleUtil.validateLocale(locale)) {
            is LocaleValidationResult.Valid -> { /* valid */ }
            is LocaleValidationResult.Invalid -> return result.error
        }
    }

    return null
}
