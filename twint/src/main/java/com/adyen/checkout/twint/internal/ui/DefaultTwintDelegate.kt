/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.twint.internal.ui

import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import ch.twint.payment.sdk.Twint
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.data.api.AnalyticsRepository
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.paymentmethod.TwintPaymentMethod
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.twint.TwintComponentState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

internal class DefaultTwintDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: OrderRequest?,
    override val componentParams: ComponentParams,
    private val analyticsRepository: AnalyticsRepository,
) : TwintDelegate {

    private val _componentStateFlow = MutableStateFlow(createComponentState())
    override val componentStateFlow: Flow<TwintComponentState> = _componentStateFlow

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val submitChannel: Channel<TwintComponentState> = bufferedChannel()
    override val submitFlow: Flow<TwintComponentState> = submitChannel.receiveAsFlow()

    override fun initialize(coroutineScope: CoroutineScope) {
        setupAnalytics(coroutineScope)

        componentStateFlow.onEach {
            onState(it)
        }.launchIn(coroutineScope)
    }

    private fun setupAnalytics(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.DEBUG) { "setupAnalytics" }
        coroutineScope.launch {
            analyticsRepository.setupAnalytics()
        }
    }

    private fun onState(state: TwintComponentState) {
        if (state.isValid) {
            submitChannel.trySend(state)
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<TwintComponentState>) -> Unit
    ) {
        observerRepository.addObservers(
            stateFlow = componentStateFlow,
            exceptionFlow = exceptionFlow,
            submitFlow = submitFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    private fun createComponentState(): TwintComponentState {
        val paymentMethod = TwintPaymentMethod(
            type = paymentMethod.type,
            subtype = "sdk",
            checkoutAttemptId = analyticsRepository.getCheckoutAttemptId(),
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod,
            order = order,
            amount = componentParams.amount,
        )

        return TwintComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true,
        )
    }

    override fun startTwintScreen(activity: ComponentActivity) {
        val twint = Twint(activity) { result ->
            when (result) {
                TwintPayResult.TW_B_SUCCESS -> TODO()
                TwintPayResult.TW_B_ERROR -> TODO()
                TwintPayResult.TW_B_APP_NOT_INSTALLED -> TODO()
            }
        }

        twint.payWithCode("test")
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onCleared() {
        removeObserver()
    }
}
