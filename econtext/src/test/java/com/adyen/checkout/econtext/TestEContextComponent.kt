/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2023.
 */

package com.adyen.checkout.econtext

import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.econtext.internal.ui.EContextDelegate

internal class TestEContextComponent internal constructor(
    delegate: EContextDelegate<TestEContextPaymentMethod, TestEContextComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<TestEContextComponentState>,
) : EContextComponent<TestEContextPaymentMethod, TestEContextComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {

    // This method is needed for testing purposes
    @Suppress("RedundantOverride")
    override fun onCleared() {
        super.onCleared()
    }
}
