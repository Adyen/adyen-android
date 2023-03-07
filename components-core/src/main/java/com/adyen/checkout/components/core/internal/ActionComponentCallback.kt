/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError

// TODO SESSIONS: docs
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionComponentCallback {
    fun onAdditionalDetails(actionComponentData: ActionComponentData)
    fun onError(componentError: ComponentError)
}
