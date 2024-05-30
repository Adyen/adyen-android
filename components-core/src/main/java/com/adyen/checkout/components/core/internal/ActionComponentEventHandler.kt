/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentCallback

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionComponentEventHandler {
    fun onActionComponentEvent(event: ActionComponentEvent, actionComponentCallback: ActionComponentCallback)
}
