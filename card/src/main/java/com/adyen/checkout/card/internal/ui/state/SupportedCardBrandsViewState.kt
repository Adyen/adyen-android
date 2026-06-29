/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/6/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.CardBrand

internal data class SupportedCardBrandsViewState(
    val supportedCardBrands: List<CardBrand>,
    // We use an isVisible flag instead of making this class nullable because we need to animate the enter/exit
    // transitions when the visibility changes. If this whole class was nullable and skipped from composition - as is
    // the case with other UI elements - then the exit animation would not play.
    val isVisible: Boolean,
)
