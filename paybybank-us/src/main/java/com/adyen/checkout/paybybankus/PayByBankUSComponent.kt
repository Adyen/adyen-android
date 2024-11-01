/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.adyen.checkout.action.core.internal.ActionHandlingComponent
import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.paybybankus.internal.PayByBankUSDelegate
import com.adyen.checkout.paybybankus.internal.provider.PayByBankUSComponentProvider
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.PAY_BY_BANK_US] payment method.
 */
// TODO remove the suppress annotation after complete implementation
@Suppress("UnusedPrivateProperty", "UnusedParameter")
class PayByBankUSComponent internal constructor(
    private val payByBankUSDelegate: PayByBankUSDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<PayByBankUSComponentState>,
) : ViewModel(),
    PaymentComponent,
    ViewableComponent,
    ActionHandlingComponent by actionHandlingComponent {

    override val delegate: ComponentDelegate
        get() = TODO("Not yet implemented")

    override val viewFlow: Flow<ComponentViewType?>
        get() = TODO("Not yet implemented")

    internal fun observe(
        lifecycleOwner: LifecycleOwner,
        callback: (PaymentComponentEvent<PayByBankUSComponentState>) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    internal fun removeObserver() {
        TODO("Not yet implemented")
    }

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onCleared() {
        super.onCleared()
        TODO("Not yet implemented")
    }

    companion object {

        @JvmField
        val PROVIDER = PayByBankUSComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.PAY_BY_BANK_US)
    }
}
