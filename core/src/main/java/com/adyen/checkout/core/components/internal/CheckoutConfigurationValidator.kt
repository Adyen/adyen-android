/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.internal.helper.ClientKeyValidator
import com.adyen.checkout.core.common.internal.helper.LocaleUtil
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.error.CheckoutError

/**
 * Validates the [CheckoutConfiguration] and returns a [CheckoutError] if validation fails.
 *
 * @return [CheckoutError] if validation fails, `null` otherwise.
 */
@Suppress("ReturnCount")
internal fun CheckoutConfiguration.validate(): CheckoutError? {
    ClientKeyValidator.validateClientKey(clientKey)?.let { return it }

    shopperLocale?.let { locale ->
        LocaleUtil.validateLocale(locale)?.let { return it }
    }

    amount?.validate()?.let { return it }

    return null
}
