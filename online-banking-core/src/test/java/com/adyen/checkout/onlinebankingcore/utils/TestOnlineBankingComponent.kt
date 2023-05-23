/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.onlinebankingcore.utils

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.onlinebankingcore.internal.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.internal.ui.OnlineBankingDelegate

internal class TestOnlineBankingComponent internal constructor(
    delegate: OnlineBankingDelegate<TestOnlineBankingPaymentMethod, TestOnlineBankingComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<TestOnlineBankingComponentState>
) : OnlineBankingComponent<TestOnlineBankingPaymentMethod, TestOnlineBankingComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {

    // This is needed for testing purposes
    @Suppress("RedundantOverride")
    override fun onCleared() {
        super.onCleared()
    }
}
