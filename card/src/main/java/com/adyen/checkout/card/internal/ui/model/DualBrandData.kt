/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2025.
 */

package com.adyen.checkout.card.internal.ui.model

internal data class DualBrandData(
    val brandOptionFirst: CardBrandItem,
    val brandOptionSecond: CardBrandItem,
)

internal val DualBrandData.selectedBrand
    get() = when {
        brandOptionFirst.isSelected -> brandOptionFirst.brand
        brandOptionSecond.isSelected -> brandOptionSecond.brand
        else -> null
    }
