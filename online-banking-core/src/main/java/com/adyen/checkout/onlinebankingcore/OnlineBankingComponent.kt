/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */
package com.adyen.checkout.onlinebankingcore

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.flow.mapToCallbackWithLifeCycle
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import kotlinx.coroutines.flow.Flow

abstract class OnlineBankingComponent<IssuerListPaymentMethodT : IssuerListPaymentMethod>(
    savedStateHandle: SavedStateHandle,
    final override val delegate: OnlineBankingDelegate<IssuerListPaymentMethodT>,
    configuration: Configuration
) : BasePaymentComponent<
    Configuration,
    PaymentComponentState<IssuerListPaymentMethodT>
    >(savedStateHandle, delegate, configuration),
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<IssuerListPaymentMethodT>>) -> Unit
    ) {
        delegate.componentStateFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope) {
            callback(PaymentComponentEvent.StateChanged(it))
        }

        delegate.exceptionFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope) {
            callback(PaymentComponentEvent.Error(ComponentError(it)))
        }
    }
}
