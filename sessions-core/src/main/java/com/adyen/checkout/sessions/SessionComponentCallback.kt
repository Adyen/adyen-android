/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 10/1/2023.
 */

package com.adyen.checkout.sessions

import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.model.payments.response.Action

interface SessionComponentCallback {
    fun onAction(action: Action)
    fun onError(componentError: ComponentError)
    fun onFinished(resultCode: String)
}
