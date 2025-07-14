/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.await.internal.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.action.internal.ActionDelegate

internal class AwaitDelegate : ActionDelegate {

    @Composable
    override fun ViewFactory(modifier: Modifier) {
        Text("I am AWAIT")
    }
}
