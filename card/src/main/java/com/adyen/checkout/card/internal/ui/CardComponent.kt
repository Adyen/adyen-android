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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.card.internal.ui.state.CardChangeListener
import com.adyen.checkout.card.internal.ui.state.CardComponentState
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.view.CardComponent
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.internal.ui.state.StateManager
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable

// TODO - Card full implementation
internal class CardComponent(
    private val stateManager: StateManager<CardViewState, CardComponentState>,
) : PaymentComponent<CardPaymentComponentState>, CardChangeListener {

    // TODO - Card. Emit events.
    override val eventFlow: Flow<PaymentComponentEvent<CardPaymentComponentState>> = flowOf()

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        CardNavKey to CheckoutNavEntry(CardNavKey) { backStack -> MainScreen(backStack) },
    )

    override val navigationStartingPoint: NavKey = CardNavKey

    override fun submit() {
        if (stateManager.isValid) {
            // TODO - Card. Create payment component state.
            adyenLog(AdyenLogLevel.DEBUG) { "CardComponent: Submit Triggered." }
        } else {
            stateManager.highlightAllValidationErrors()
        }
    }

    override fun setLoading(isLoading: Boolean) {
        stateManager.updateViewState {
            copy(isLoading = isLoading)
        }
    }

    override fun onCardNumberChanged(newCardNumber: String) {
        stateManager.updateViewStateAndValidate {
            copy(
                cardNumber = cardNumber.updateText(newCardNumber),
            )
        }
    }

    override fun onCardNumberFocusChanged(hasFocus: Boolean) {
        stateManager.updateViewState {
            copy(
                cardNumber = cardNumber.updateFocus(hasFocus),
            )
        }
    }

    @Composable
    private fun MainScreen(@Suppress("UNUSED_PARAMETER") backStack: NavBackStack<NavKey>) {
        val viewState by stateManager.viewState.collectAsStateWithLifecycle()
        CardComponent(
            viewState = viewState,
            changeListener = this,
            onSubmitClick = ::submit,
        )
    }
}

@Serializable
private data object CardNavKey : NavKey
