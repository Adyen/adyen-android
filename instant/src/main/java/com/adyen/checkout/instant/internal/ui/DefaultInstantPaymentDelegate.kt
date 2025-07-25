/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2022.
 */

package com.adyen.checkout.instant.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.ActionHandlingMethod
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.GenericPaymentMethod
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.instant.InstantComponentState
import com.adyen.checkout.instant.internal.ui.model.InstantComponentParams
import com.adyen.checkout.ui.core.old.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class DefaultInstantPaymentDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    override val componentParams: InstantComponentParams,
    private val analyticsManager: AnalyticsManager,
) : InstantPaymentDelegate {

    override val componentStateFlow: StateFlow<InstantComponentState> = MutableStateFlow(createComponentState())

    private val submitChannel: Channel<InstantComponentState> = bufferedChannel()
    override val submitFlow: Flow<InstantComponentState> = submitChannel.receiveAsFlow()

    private val _viewFlow = MutableStateFlow<ComponentViewType>(PaymentInProgressViewType)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    init {
        submitChannel.trySend(componentStateFlow.value)
    }

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    private fun createComponentState(): InstantComponentState {
        val paymentComponentData = PaymentComponentData<PaymentMethodDetails>(
            paymentMethod = GenericPaymentMethod(
                type = paymentMethod.type,
                checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
                subtype = getSubtype(paymentMethod),
            ),
            order = order,
            amount = componentParams.amount,
        )
        return InstantComponentState(paymentComponentData, isInputValid = true, isReady = true)
    }

    private fun getSubtype(paymentMethod: PaymentMethod): String? {
        return when (componentParams.actionHandlingMethod) {
            ActionHandlingMethod.PREFER_NATIVE -> {
                when (paymentMethod.type) {
                    PaymentMethodTypes.TWINT -> SDK_SUBTYPE
                    else -> null
                }
            }

            ActionHandlingMethod.PREFER_WEB -> null
        }
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
        callback: (PaymentComponentEvent<InstantComponentState>) -> Unit
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

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }

    companion object {
        private const val SDK_SUBTYPE = "sdk"
    }
}
