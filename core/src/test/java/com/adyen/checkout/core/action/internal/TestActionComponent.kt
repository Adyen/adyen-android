/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class TestActionComponent : ActionComponent {

    override val navigation: Map<NavKey, CheckoutNavEntry> = emptyMap()

    override val navigationStartingPoint: NavKey
        get() = error("Not implemented for testing")

    override fun handleAction() {
        // No-op
    }

    override val eventFlow: Flow<ActionComponentEvent>
        get() = flowOf()
}
