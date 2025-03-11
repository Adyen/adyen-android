/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.StateUpdater
import com.adyen.checkout.components.core.internal.ui.model.state.StateUpdaterRegistry

internal class CardStateUpdaterRegistry : StateUpdaterRegistry<CardFieldId, CardDelegateState> {

    private val updaters = CardFieldId.entries.associateWith { fieldId ->
        when (fieldId) {
            CardFieldId.CARD_NUMBER -> CardNumberUpdater()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getFieldState(key: CardFieldId, state: CardDelegateState): ComponentFieldDelegateState<T> {
        val updater = updaters[key] as? StateUpdater<CardDelegateState, ComponentFieldDelegateState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.getFieldState(state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> updateFieldState(
        key: CardFieldId,
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<T>
    ): CardDelegateState {
        val updater = updaters[key] as? StateUpdater<CardDelegateState, ComponentFieldDelegateState<T>>
            ?: throw IllegalArgumentException("Unsupported fieldId or invalid type provided")
        return updater.updateFieldState(state, fieldState)
    }
}

internal class CardNumberUpdater : StateUpdater<CardDelegateState, ComponentFieldDelegateState<String>> {
    override fun getFieldState(state: CardDelegateState): ComponentFieldDelegateState<String> = state.cardNumberDelegateState

    override fun updateFieldState(
        state: CardDelegateState,
        fieldState: ComponentFieldDelegateState<String>
    ) = state.copy(
        cardNumberDelegateState = fieldState,
    )
}
