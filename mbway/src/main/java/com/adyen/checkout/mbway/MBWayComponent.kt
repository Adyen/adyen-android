/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.mbway

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.mbway.MBWayComponent.Companion.PROVIDER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class MBWayComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: MBWayDelegate,
    configuration: MBWayConfiguration
) :
    BasePaymentComponent<MBWayConfiguration,
        PaymentComponentState<MBWayPaymentMethod>>(savedStateHandle, delegate, configuration),
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PaymentComponentState<MBWayPaymentMethod>>) -> Unit
    ) {
        delegate.componentStateFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { callback(PaymentComponentEvent.StateChanged(it)) }
            .launchIn(viewModelScope)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<MBWayComponent, MBWayConfiguration> = MBWayComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.MB_WAY)
    }
}
