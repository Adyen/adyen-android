/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayPaymentComponentState
import kotlinx.coroutines.flow.Flow

internal class GooglePayComponent : PaymentComponent<GooglePayPaymentComponentState> {

    override fun submit() {
        TODO("Not yet implemented")
    }

    override fun setLoading(isLoading: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onCleared() {
        TODO("Not yet implemented")
    }

    override val eventFlow: Flow<PaymentComponentEvent<GooglePayPaymentComponentState>>
        get() = TODO("Not yet implemented")
    override val navigation: Map<NavKey, CheckoutNavEntry>
        get() = TODO("Not yet implemented")
    override val navigationStartingPoint: NavKey
        get() = TODO("Not yet implemented")
}
