/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/11/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.common.CardBrand

internal data class CardBrandItem(
    val name: String,
    val brand: CardBrand,
    val isSelected: Boolean,
)
