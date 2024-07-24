/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/7/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class ExpiryDateValidationResult {
    VALID,
    INVALID_TOO_FAR_IN_THE_FUTURE,
    INVALID_TOO_OLD,
    INVALID_EXPIRY_DATE
}
