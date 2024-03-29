/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.issuerlist.utils

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.issuerlist.TestIssuerComponentState
import com.adyen.checkout.issuerlist.internal.IssuerListComponent
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

internal class TestIssuerListComponent internal constructor(
    delegate: IssuerListDelegate<TestIssuerPaymentMethod, TestIssuerComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<TestIssuerComponentState>,
) : IssuerListComponent<TestIssuerPaymentMethod, TestIssuerComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler,
) {

    // This method is needed for testing purposes
    @Suppress("RedundantOverride")
    override fun onCleared() {
        super.onCleared()
    }
}
