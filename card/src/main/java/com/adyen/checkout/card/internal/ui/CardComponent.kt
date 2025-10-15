/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.view.CardComponent
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.ui.internal.ComponentScaffold
import kotlinx.coroutines.flow.Flow

// TODO - Card full implementation
internal class CardComponent : PaymentComponent<CardPaymentComponentState> {

    override val eventFlow: Flow<PaymentComponentEvent<CardPaymentComponentState>>
        get() = TODO("Not yet implemented")

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        ComponentScaffold(
            modifier = modifier,
        ) {
            CardComponent()
        }
    }

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun setLoading(isLoading: Boolean) {
        TODO("Not yet implemented")
    }
}
