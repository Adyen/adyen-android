/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.onlinebankingcore.utils

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.OnlineBankingDelegate

internal class TestOnlineBankingComponent internal constructor(
    delegate: OnlineBankingDelegate<TestOnlineBankingPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<PaymentComponentState<TestOnlineBankingPaymentMethod>>
) : OnlineBankingComponent<TestOnlineBankingPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? OnlineBankingDelegate<*>)?.setInteractionBlocked(isInteractionBlocked)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
