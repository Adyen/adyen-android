/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/1/2023.
 */

package com.adyen.checkout.components.base

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentState

// TODO SESSIONS: docs
interface ComponentCallback<T : PaymentComponentState<*>> {
    // Generic events
    fun onStateChanged(state: T) = Unit
    fun onSubmit(state: T)
    fun onAdditionalDetails(actionComponentData: ActionComponentData)
    fun onError(componentError: ComponentError)
}
