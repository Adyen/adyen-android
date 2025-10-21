/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/5/2024.
 */

package com.adyen.checkout.ideal.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.provider.SdkDataProvider
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.IdealPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ideal.IdealComponentState
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("LongParameterList")
internal class DefaultIdealDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    override val componentParams: GenericComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val sdkDataProvider: SdkDataProvider,
) : IdealDelegate {

    override val componentStateFlow: StateFlow<IdealComponentState> = MutableStateFlow(createComponentState())

    private val submitChannel: Channel<IdealComponentState> = bufferedChannel()
    override val submitFlow: Flow<IdealComponentState> = submitChannel.receiveAsFlow()

    private val _viewFlow = MutableStateFlow<ComponentViewType>(PaymentInProgressViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    init {
        submitChannel.trySend(componentStateFlow.value)
    }

    override fun initialize(coroutineScope: CoroutineScope) {
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val renderedEvent = GenericEvents.rendered(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(renderedEvent)

        val submitEvent = GenericEvents.submit(paymentMethod.type.orEmpty())
        analyticsManager.trackEvent(submitEvent)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<IdealComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = null,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    private fun createComponentState(): IdealComponentState {
        val paymentComponentData = PaymentComponentData(
            paymentMethod = IdealPaymentMethod(
                type = paymentMethod.type,
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                // Set issuer to null to force redirect flow (ideal 2.0)
                issuer = null,
            ),
            order = order,
            amount = componentParams.amount,
            sdkData = sdkDataProvider.createEncodedSdkData(),
        )
        return IdealComponentState(paymentComponentData, isInputValid = true, isReady = true)
    }

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
