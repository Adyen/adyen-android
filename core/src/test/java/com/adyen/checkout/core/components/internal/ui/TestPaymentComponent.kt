/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/7/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class TestPaymentComponent(
    override val eventFlow: Flow<PaymentComponentEvent> = flowOf(),
    private val requiresUserInteraction: Boolean = true,
) : PaymentComponent {

    var submitCount = 0
        private set

    var isLoading = false
        private set

    @Composable
    override fun Content(modifier: Modifier) = Unit

    override fun submit() {
        submitCount++
    }

    override fun requiresUserInteraction(): Boolean = requiresUserInteraction

    override fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
    }

    override fun onCleared() = Unit
}
