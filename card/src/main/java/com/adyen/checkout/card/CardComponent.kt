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
import com.adyen.checkout.action.ActionHandlingComponent
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.card.CardComponent.Companion.PROVIDER
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.toActionCallback
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class CardComponent internal constructor(
    savedStateHandle: SavedStateHandle,
    val cardDelegate: CardDelegate,
    cardConfiguration: CardConfiguration,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent
) :
    BasePaymentComponent<CardConfiguration, CardComponentState>(
        savedStateHandle,
        cardDelegate,
        cardConfiguration
    ),
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?>
        get() = merge(
            cardDelegate.viewFlow,
            genericActionDelegate.viewFlow,
        )

    init {
        cardDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<CardComponentState>) -> Unit
    ) {
        cardDelegate.observe(lifecycleOwner, viewModelScope, callback)

        genericActionDelegate.observe(
            lifecycleOwner,
            viewModelScope,
            callback.toActionCallback(),
        )
    }

    override fun removeObserver() {
        cardDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun requiresInput() = cardDelegate.requiresInput()

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        cardDelegate.onCleared()
        genericActionDelegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: StoredPaymentComponentProvider<CardComponent, CardConfiguration> = CardComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SCHEME)
    }
}
