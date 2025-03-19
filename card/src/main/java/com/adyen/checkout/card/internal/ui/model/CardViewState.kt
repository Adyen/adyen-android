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
import com.adyen.checkout.core.ui.model.ExpiryDate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardViewState(
    val cardNumberFieldState: ComponentFieldViewState<String>,
    val cardSecurityCodeFieldState: ComponentFieldViewState<String>,
    val cardExpiryDateFieldState: ComponentFieldViewState<ExpiryDate>,
)

internal fun CardDelegateState.toViewState() = CardViewState(
    cardNumberFieldState = this.cardNumberDelegateState.toComponentFieldViewState(),
    cardSecurityCodeFieldState = this.cardSecurityCodeDelegateState.toComponentFieldViewState(),
    cardExpiryDateFieldState = this.cardExpiryDateDelegateState.toComponentFieldViewState(),
)
