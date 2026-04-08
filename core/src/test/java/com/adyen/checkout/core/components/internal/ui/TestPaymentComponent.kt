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
import com.adyen.checkout.core.components.internal.BasePaymentComponentState
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class TestPaymentComponent : PaymentComponent<BasePaymentComponentState> {

    override val eventFlow: Flow<PaymentComponentEvent<BasePaymentComponentState>>
        get() = flowOf()

    @Composable
    override fun Content(modifier: Modifier) = Unit

    override fun submit() = Unit

    override fun setLoading(isLoading: Boolean) = Unit

    override fun onCleared() = Unit
}
