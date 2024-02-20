/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2024.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.action.Action

internal class TestSessionComponentCallback : SessionComponentCallback<TestComponentState> {
    override fun onAction(action: Action) {
        // Not necessary
    }

    override fun onFinished(result: SessionPaymentResult) {
        // Not necessary
    }

    override fun onError(componentError: ComponentError) {
        // Not necessary
    }
}
