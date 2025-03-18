/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.components.core.internal.ui.model.toComponentFieldViewState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardViewState(
    val cardNumberFieldState: ComponentFieldViewState<String>,
    val securityCodeFieldState: ComponentFieldViewState<String>,
)

internal fun CardDelegateState.toViewState() = CardViewState(
    cardNumberFieldState = this.cardNumberDelegateState.toComponentFieldViewState(),
    securityCodeFieldState = this.securityCodeDelegateState.toComponentFieldViewState(),
)
