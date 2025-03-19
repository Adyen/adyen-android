/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.CardDelegateState
import com.adyen.checkout.card.internal.ui.model.CardFieldId
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateStateFactory

// TODO: Implement
class CardDelegateStateFactory(
    private val componentParams: CardComponentParams
) : DelegateStateFactory<CardDelegateState, CardFieldId> {

    override fun createDefaultDelegateState() = CardDelegateState(componentParams = componentParams)

    override fun getFieldIds(): List<CardFieldId> = CardFieldId.entries
}
