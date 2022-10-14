/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.sepa

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.sepa.SepaComponent.Companion.PROVIDER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class SepaComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: SepaDelegate,
    configuration: SepaConfiguration,
) : BasePaymentComponent<SepaConfiguration, PaymentComponentState<SepaPaymentMethod>>(
    savedStateHandle,
    delegate,
    configuration
),
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    init {
        delegate.componentStateFlow
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<SepaComponent, SepaConfiguration> = SepaComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SEPA)
    }
}
