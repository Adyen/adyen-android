/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 31/7/2024.
 */

package com.adyen.checkout.twint.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.internal.ui.model.TwintComponentParams
import com.adyen.checkout.twint.internal.ui.model.TwintInputData
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

@Suppress("unused")
internal class StoredTwintDelegate(
    private val analyticsManager: AnalyticsManager,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: StoredPaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: TwintComponentParams,
) : TwintDelegate {

    override val componentStateFlow: Flow<TwintComponentState> = flow { }

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(null)

    override val submitFlow: Flow<TwintComponentState> = flow { }

    override fun initialize(coroutineScope: CoroutineScope) {
        // TODO
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<TwintComponentState>) -> Unit
    ) {
        // TODO
    }

    override fun removeObserver() {
        // TODO
    }

    override fun updateInputData(update: TwintInputData.() -> Unit) {
        // TODO
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
