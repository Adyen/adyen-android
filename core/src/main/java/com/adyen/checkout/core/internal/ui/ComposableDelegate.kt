/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal interface ComposableDelegate {

    @Composable
    fun ViewFactory(modifier: Modifier)
}
