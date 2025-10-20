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
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardViewState
import com.adyen.checkout.card.internal.ui.view.CardComponent
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.state.ViewStateManager
import com.adyen.checkout.ui.internal.ComponentScaffold
import kotlinx.coroutines.flow.Flow

// TODO - Card full implementation
internal class CardComponent(
    private val viewStateManager: ViewStateManager<CardViewState>,
) : PaymentComponent<CardPaymentComponentState>, CardChangeListener {

    override val eventFlow: Flow<PaymentComponentEvent<CardPaymentComponentState>>
        get() = TODO("Not yet implemented")

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        val viewState by viewStateManager.state.collectAsStateWithLifecycle()
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
        viewStateManager.update {
            copy(
                cardNumber = cardNumber.updateText(newCardNumber),
            )
        }
    }

    override fun onCardNumberFocusChanged(hasFocus: Boolean) {
        viewStateManager.update {
            copy(
                cardNumber = cardNumber.updateFocus(hasFocus),
            )
        }
    }
}
