/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.annotation.RestrictTo
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.components.internal.ui.EventComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionComponent : EventComponent<ActionComponentEvent> {

    val navigation: Map<NavKey, CheckoutNavEntry>

    val navigationStartingPoint: NavKey

    fun handleAction()
}
