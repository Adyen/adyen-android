/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.card.internal.ui.state.CardChangeListener
import com.adyen.checkout.card.internal.ui.state.CardComponentState
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.state.CardViewStateValidator
import com.adyen.checkout.card.internal.ui.view.CardComponent
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.state.StateManager
import com.adyen.checkout.ui.internal.ComponentScaffold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// TODO - Card full implementation
internal class CardComponent(
    private val stateManager: StateManager<CardViewState, CardComponentState>,
    private val validator: CardViewStateValidator,
) : PaymentComponent<CardPaymentComponentState>, CardChangeListener {

    override val eventFlow: Flow<PaymentComponentEvent<CardPaymentComponentState>>
        get() = TODO("Not yet implemented")

    internal fun initialize(coroutineScope: CoroutineScope) {
        stateManager.componentState.onEach {
            val newState = validator.validate(stateManager.viewState.value, it)
            stateManager.updateViewState {
                copy(
                    cardNumber = newState.cardNumber,
                    isAmex = newState.isAmex,
                )
            }
        }.launchIn(coroutineScope)
    }

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        val viewState by stateManager.viewState.collectAsStateWithLifecycle()
        ComponentScaffold(
            modifier = modifier,
        ) {
            CardComponent(
                viewState = viewState,
                changeListener = this,
            )
        }
    }

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun setLoading(isLoading: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onCardNumberChanged(newCardNumber: String) {
        stateManager.updateViewState {
            copy(
                cardNumber = cardNumber.updateText(newCardNumber),
            )
        }
    }
}
