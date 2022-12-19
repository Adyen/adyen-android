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
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.OnlineBankingDelegate

internal class TestOnlineBankingComponent internal constructor(
    delegate: OnlineBankingDelegate<TestOnlineBankingPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
) : OnlineBankingComponent<TestOnlineBankingPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
) {
    override fun onCleared() {
        super.onCleared()
    }
}
