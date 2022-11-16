/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.card

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.card.CardComponent.Companion.PROVIDER
import com.adyen.checkout.components.ActionHandlingComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class CardComponent internal constructor(
    savedStateHandle: SavedStateHandle,
    override val delegate: CardDelegate,
    cardConfiguration: CardConfiguration,
    private val actionHandlingDelegate: DefaultActionHandlingComponent
) :
    BasePaymentComponent<CardConfiguration, CardComponentState>(
        savedStateHandle,
        delegate,
        cardConfiguration
    ),
    ViewableComponent,
    ActionHandlingComponent by actionHandlingDelegate {

    override val viewFlow: Flow<ComponentViewType?> get() = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<CardComponentState>) -> Unit
    ) {
        delegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    override fun removeObserver() {
        delegate.removeObserver()
    }

    override fun requiresInput() = delegate.requiresInput()

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        delegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
    }
}
