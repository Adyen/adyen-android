/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/4/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.CardBrand

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class DualBrandData(
    val selectedBrand: CardBrand?,
    val brandOptions: List<CardBrandItem>,
    val selectable: Boolean
)
