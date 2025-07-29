/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.action.data.ActionComponentData

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class ActionComponentEvent {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    class ActionDetails(
        val data: ActionComponentData
    ) : ActionComponentEvent()
}
