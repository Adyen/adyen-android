/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 12/12/2022.
 */

package com.adyen.checkout.card.data

enum class RestrictedCardType(val txVariant: String) {
    ACCEL("accel"),
    PULSE("pulse"),
    STAR("star"),
    NYCE("nyce");

    companion object {
        fun isRestrictedCardType(brand: String) = values().any { brand == it.txVariant }
    }
}
