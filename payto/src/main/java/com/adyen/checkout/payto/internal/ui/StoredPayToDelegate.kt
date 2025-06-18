/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/2/2025.
 */

package com.adyen.checkout.payto.internal.ui

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.PayToPaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.payto.PayToComponentState
import com.adyen.checkout.payto.internal.ui.model.PayIdTypeModel
import com.adyen.checkout.payto.internal.ui.model.PayToInputData
import com.adyen.checkout.payto.internal.ui.model.PayToMode
import com.adyen.checkout.payto.internal.ui.model.PayToOutputData
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("TooManyFunctions")
internal class StoredPayToDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val storedPaymentMethod: StoredPaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
) : PayToDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<PayToOutputData> = _outputDataFlow

    override val outputData: PayToOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<PayToComponentState> = _componentStateFlow

    private val _viewFlow: MutableStateFlow<ComponentViewType?> = MutableStateFlow(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private val submitChannel = bufferedChannel<PayToComponentState>()
    override val submitFlow: Flow<PayToComponentState> = submitChannel.receiveAsFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        initializeAnalytics(coroutineScope)

        componentStateFlow.onEach {
            onState(it)
        }.launchIn(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.VERBOSE) { "initializeAnalytics" }
        analyticsManager.initialize(this, coroutineScope)

        val event = GenericEvents.rendered(
            component = storedPaymentMethod.type.orEmpty(),
            isStoredPaymentMethod = true,
        )
        analyticsManager.trackEvent(event)
    }

    private fun onState(payToComponentState: PayToComponentState) {
        if (payToComponentState.isValid) {
            val event = GenericEvents.submit(storedPaymentMethod.type.orEmpty())
            analyticsManager.trackEvent(event)

            submitChannel.trySend(payToComponentState)
        }
    }

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PayToComponentState>) -> Unit
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

    override fun getPayIdTypes(): List<PayIdTypeModel> {
        adyenLog(AdyenLogLevel.WARN) { "getPayIdTypes should not be called for stored PayTo" }
        return emptyList()
    }

    override fun updateInputData(update: PayToInputData.() -> Unit) {
        adyenLog(AdyenLogLevel.WARN) { "updateInputData should not be called for stored PayTo" }
    }

    private fun createOutputData(): PayToOutputData {
        // No information will be displayed
        return PayToOutputData(
            mode = PayToMode.PAY_ID,
            payIdTypeModel = null,
            mobilePhoneNumber = "",
            emailAddress = "",
            abnNumber = "",
            organizationId = "",
            bsbAccountNumber = "",
            bsbStateBranch = "",
            firstName = "",
            lastName = "",
        )
    }

    // TODO Here we only call this method on initialization. The checkoutAttemptId will only be available if it is
    //  passed by drop-in. This should be fixed as part of state refactoring.
    private fun createComponentState(): PayToComponentState {
        val paymentMethod = PayToPaymentMethod(
            type = getPaymentMethodType(),
            checkoutAttemptId = analyticsManager.getCheckoutAttemptId(),
            storedPaymentMethodId = storedPaymentMethod.id,
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return PayToComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
        )
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        // no ops
    }

    override fun onCleared() {
        removeObserver()
        analyticsManager.clear(this)
    }
}
