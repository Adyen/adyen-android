/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */
package com.adyen.checkout.issuerlist

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly.
 */
abstract class IssuerListComponent<IssuerListPaymentMethodT : IssuerListPaymentMethod> protected constructor(
    savedStateHandle: SavedStateHandle,
    final override val delegate: IssuerListDelegate<IssuerListPaymentMethodT>,
    configuration: IssuerListConfiguration
) : BasePaymentComponent<
    IssuerListConfiguration,
    PaymentComponentState<IssuerListPaymentMethodT>
    >(
    savedStateHandle,
    delegate,
    configuration
),
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<IssuerListPaymentMethodT>>) -> Unit
    ) {
        delegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    override fun removeObserver() {
        delegate.removeObserver()
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        delegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
