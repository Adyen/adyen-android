/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/4/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.Environment

internal data class CardBrandItem(
    val name: String,
    val brand: CardBrand,
    val isSelected: Boolean,
    // We need the environment to load the logo
    val environment: Environment,
)
