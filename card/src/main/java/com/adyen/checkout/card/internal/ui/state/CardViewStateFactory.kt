/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ViewStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState

internal class CardViewStateFactory : ViewStateFactory<CardViewState> {

    // TODO - Card Full Implementation
    override fun createDefaultViewState() = CardViewState(
        cardNumber = TextInputState(isFocused = true),
        isAmex = false,
    )
}
