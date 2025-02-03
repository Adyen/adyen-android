/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/2/2025.
 */

package com.adyen.checkout.payto.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.payto.PayToComponentState
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach

// TODO Remove the unnecessary annotations
@Suppress("TooManyFunctions")
internal class DefaultPayToDelegate(
    @Suppress("UnusedPrivateProperty")
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    @Suppress("UnusedPrivateProperty")
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<PayToComponentState>,
) : PayToDelegate {

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(PayToComponentViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    override val uiStateFlow: Flow<PaymentComponentUIState> = submitHandler.uiStateFlow

    override val uiEventFlow: Flow<PaymentComponentUIEvent> = submitHandler.uiEventFlow

    private fun getTrackedSubmitFlow() = submitHandler.submitFlow.onEach {
        val event = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(event)
    }

    override fun onSubmit() {
        TODO("Not yet implemented")
    }

    override fun isConfirmationRequired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun shouldShowSubmitButton(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        TODO("Not yet implemented")
    }

    override val submitFlow: Flow<PayToComponentState> = getTrackedSubmitFlow()
    override fun getPaymentMethodType(): String {
        TODO("Not yet implemented")
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PayToComponentState>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeObserver() {
        TODO("Not yet implemented")
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        TODO("Not yet implemented")
    }

    override fun onCleared() {
        TODO("Not yet implemented")
    }
}
