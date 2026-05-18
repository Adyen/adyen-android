/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/5/2026.
 */

package com.adyen.checkout.card.scanning.internal

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardScanResult(
    val pan: String?,
    val expiryMonth: Int?,
    val expiryYear: Int?,
)
