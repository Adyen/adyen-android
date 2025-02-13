/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 13/2/2025.
 */

package com.adyen.checkout.card.scanning

data class AdyenCardScannerResult(
    val pan: String?,
    val expiryMonth: Int?,
    val expiryYear: Int?,
)
