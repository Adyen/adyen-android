/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.ui.ViewProvidingComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.giftcard.GiftCardComponent.Companion.PROVIDER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class GiftCardComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: GiftCardDelegate,
    configuration: GiftCardConfiguration,
) : BasePaymentComponent<GiftCardConfiguration, GiftCardInputData, GiftCardOutputData, GiftCardComponentState>(
    savedStateHandle,
    delegate,
    configuration
),
    ViewProvidingComponent {

    override val viewFlow: Flow<ComponentViewType?> get() = delegate.viewFlow

    override val inputData: GiftCardInputData
        get() = delegate.inputData

    init {
        delegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        delegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)

        delegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)

        delegate.initialize(viewModelScope)
    }

    override fun onInputDataChanged(inputData: GiftCardInputData) {
        delegate.onInputDataChanged(inputData)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> = GiftCardComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GIFTCARD)
    }
}
