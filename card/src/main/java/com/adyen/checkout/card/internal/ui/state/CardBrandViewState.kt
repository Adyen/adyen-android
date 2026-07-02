/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/6/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.core.common.CardBrand

internal sealed class CardBrandViewState {
    data object Placeholder : CardBrandViewState()
    data class SingleBrand(val brand: CardBrand) : CardBrandViewState()

    @Immutable
    data class DualBrand(val brands: List<CardBrand>) : CardBrandViewState()

    @Immutable
    data class SelectableDualBrand(val brands: List<SelectableCardBrandItem>) : CardBrandViewState()
}

internal data class SelectableCardBrandItem(
    val brand: CardBrand,
    val isSelected: Boolean,
)
