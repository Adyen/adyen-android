/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/10/2022.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.CardBrand
import com.adyen.checkout.core.Environment

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardListItem(
    val cardBrand: CardBrand,
    val isDetected: Boolean,
    // We need the environment to load the logo
    val environment: Environment,
)
