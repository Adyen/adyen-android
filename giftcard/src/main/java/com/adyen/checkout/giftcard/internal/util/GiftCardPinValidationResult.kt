/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/7/2024.
 */

package com.adyen.checkout.giftcard.internal.util

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class GiftCardPinValidationResult {
    VALID,
    INVALID,
}
