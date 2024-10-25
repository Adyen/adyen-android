/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus.internal

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.PaymentObserverRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.ui.model.ButtonComponentParams
import com.adyen.checkout.paybybankus.PayByBankUSComponentState
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIEvent
import com.adyen.checkout.ui.core.internal.ui.PaymentComponentUIState
import com.adyen.checkout.ui.core.internal.ui.SubmitHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

// TODO remove the suppress annotation after complete implementation
@Suppress("UnusedPrivateProperty", "UnusedParameter")
internal class DefaultPayByBankUSDelegate(
    private val observerRepository: PaymentObserverRepository,
    private val paymentMethod: PaymentMethod,
    private val order: Order?,
    override val componentParams: ButtonComponentParams,
    private val analyticsManager: AnalyticsManager,
    private val submitHandler: SubmitHandler<PayByBankUSComponentState>,
) : PayByBankUSDelegate {

    override val componentStateFlow: Flow<PayByBankUSComponentState>
        get() = TODO("Not yet implemented")

    override val viewFlow: Flow<ComponentViewType?>
        get() = TODO("Not yet implemented")

    override val submitFlow: Flow<PayByBankUSComponentState>
        get() = TODO("Not yet implemented")

    override val uiStateFlow: Flow<PaymentComponentUIState>
        get() = TODO("Not yet implemented")

    override val uiEventFlow: Flow<PaymentComponentUIEvent>
        get() = TODO("Not yet implemented")

    override fun initialize(coroutineScope: CoroutineScope) {
        TODO("Not yet implemented")
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<PayByBankUSComponentState>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun removeObserver() {
        TODO("Not yet implemented")
    }

    override fun getPaymentMethodType(): String {
        TODO("Not yet implemented")
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

    override fun onCleared() {
        TODO("Not yet implemented")
    }
}
