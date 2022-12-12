/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.action.ActionHandlingComponent
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponent
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.extensions.mergeViewFlows
import com.adyen.checkout.components.toActionCallback
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.giftcard.GiftCardComponent.Companion.PROVIDER
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class GiftCardComponent internal constructor(
    private val giftCardDelegate: GiftCardDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
) : ViewModel(),
    PaymentComponent<GiftCardComponentState>,
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate get() = actionHandlingComponent.activeDelegate

    override val viewFlow: Flow<ComponentViewType?> = mergeViewFlows(
        viewModelScope,
        giftCardDelegate.viewFlow,
        genericActionDelegate.viewFlow,
    )

    init {
        giftCardDelegate.initialize(viewModelScope)
        genericActionDelegate.initialize(viewModelScope)
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<GiftCardComponentState>) -> Unit
    ) {
        giftCardDelegate.observe(lifecycleOwner, viewModelScope, callback)
        genericActionDelegate.observe(lifecycleOwner, viewModelScope, callback.toActionCallback())
    }

    override fun removeObserver() {
        giftCardDelegate.removeObserver()
        genericActionDelegate.removeObserver()
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        giftCardDelegate.onCleared()
        genericActionDelegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> = GiftCardComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GIFTCARD)
    }
}
