/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/2/2024.
 */

package com.adyen.checkout.components.core

internal class TestComponentCallback : ComponentCallback<TestComponentState> {
    override fun onSubmit(state: TestComponentState) {
        // Not necessary
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        // Not necessary
    }

    override fun onError(componentError: ComponentError) {
        // Not necessary
    }
}
