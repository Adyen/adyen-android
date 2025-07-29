/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class TestActionComponent : ActionComponent {
    @Composable
    override fun ViewFactory(modifier: Modifier) {
        // No-op
    }

    override val eventFlow: Flow<ActionComponentEvent>
        get() = flowOf()
}
