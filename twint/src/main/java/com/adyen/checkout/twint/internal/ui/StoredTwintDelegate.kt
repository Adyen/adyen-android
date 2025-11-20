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
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.provider.SdkDataProvider
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.TwintPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.twint.TwintComponentState
import com.adyen.checkout.twint.internal.ui.model.TwintComponentParams
import com.adyen.checkout.twint.internal.ui.model.TwintInputData
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

internal class StoredTwintDelegate(
    private val analyticsManager: AnalyticsManager,
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: StoredPaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: TwintComponentParams,
    private val sdkDataProvider: SdkDataProvider,
) : TwintDelegate {

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<TwintComponentState> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(null)

    private val submitChannel = bufferedChannel<TwintComponentState>()
    override val submitFlow: Flow<TwintComponentState> = submitChannel.receiveAsFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        initializeAnalytics(coroutineScope)

        componentStateFlow
            .onEach { onState(it) }
            .launchIn(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(
            component = paymentMethod.type.orEmpty(),
            isStoredPaymentMethod = true,
        )
        analyticsManager.trackEvent(event)
    }

    private fun onState(componentState: TwintComponentState) {
        if (componentState.isValid) {
            val event = GenericEvents.submit(paymentMethod.type.orEmpty())
            analyticsManager.trackEvent(event)

            submitChannel.trySend(componentState)
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<TwintComponentState>) -> Unit
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

    override fun updateInputData(update: TwintInputData.() -> Unit) {
        adyenLog(AdyenLogLevel.WARN) { "updateInputData should not be called for stored Twint" }
    }

    @Suppress("ForbiddenComment")
    // TODO: Here we only call this method on initialization. The checkoutAttemptId will only be available if it is
    //  passed by drop-in. This should be fixed as part of state refactoring.
    private fun createComponentState(): TwintComponentState {
        val twintPaymentMethod = TwintPaymentMethod(
            type = paymentMethod.type,
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            storedPaymentMethodId = paymentMethod.id,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = twintPaymentMethod,
            order = order,
            amount = componentParams.amount,
            sdkData = sdkDataProvider.createEncodedSdkData(),
        )

        return TwintComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
        )
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
