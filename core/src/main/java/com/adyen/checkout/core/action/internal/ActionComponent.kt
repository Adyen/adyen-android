/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.ComposableComponent
import com.adyen.checkout.core.components.internal.ui.EventComponent

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionComponent : ComposableComponent, EventComponent<ActionComponentEvent> {
    fun handleAction(context: Context)
}
