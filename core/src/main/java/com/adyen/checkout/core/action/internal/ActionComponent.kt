/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.components.internal.ui.EventComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionComponent : EventComponent<ActionComponentEvent> {

    val componentParams: ComponentParams

    @Composable
    fun ViewFactory(modifier: Modifier)

    fun handleAction()
}
