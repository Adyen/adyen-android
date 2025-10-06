/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.ui.model

internal enum class RestrictedCardType(val txVariant: String) {
    ACCEL("accel"),
    PULSE("pulse"),
    STAR("star"),
    NYCE("nyce");

    companion object {
        fun isRestrictedCardType(brand: String) = values().any { brand == it.txVariant }
    }
}
