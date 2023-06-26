/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.cashapppay.internal.ui.model.CashAppPayComponentParams
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultCashAppPayDelegate(
    private val analyticsRepository: AnalyticsRepository,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: CashAppPayComponentParams,
) : CashAppPayDelegate {

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(CashAppPayComponentViewType)

    override val submitFlow: Flow<CashAppPayComponentState>
        get() = TODO("Not yet implemented")

    override fun initialize(coroutineScope: CoroutineScope) {
        // TODO
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<CashAppPayComponentState>) -> Unit
    ) {
        // TODO
    }

    override fun removeObserver() {
        // TODO
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        // TODO
    }
}
