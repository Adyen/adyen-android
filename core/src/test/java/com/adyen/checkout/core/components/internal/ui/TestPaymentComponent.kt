/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/7/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.components.internal.BasePaymentComponentState
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class TestPaymentComponent : PaymentComponent<BasePaymentComponentState> {

    override val navigation: Map<NavKey, CheckoutNavEntry> = emptyMap()

    override val navigationStartingPoint: NavKey
        get() = error("Not implemented for testing")

    override fun submit() {
        // No-op
    }

    override val eventFlow: Flow<PaymentComponentEvent<BasePaymentComponentState>>
        get() = flowOf()

    override fun setLoading(isLoading: Boolean) = Unit
}
