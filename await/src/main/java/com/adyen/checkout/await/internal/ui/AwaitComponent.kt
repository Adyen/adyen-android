/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.await.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.await.internal.ui.view.AwaitComponent
import com.adyen.checkout.core.action.internal.ActionComponent

internal class AwaitComponent : ActionComponent {

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        AwaitComponent(modifier = modifier)
    }
}
