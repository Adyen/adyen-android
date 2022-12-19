/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2022.
 */

package com.adyen.checkout.issuerlist.utils

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate

internal class TestIssuerListComponent internal constructor(
    delegate: IssuerListDelegate<TestIssuerPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
) : IssuerListComponent<TestIssuerPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
) {
    override fun onCleared() {
        super.onCleared()
    }
}
