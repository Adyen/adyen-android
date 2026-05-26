/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2025.
 */

package com.adyen.checkout.threeds2.internal.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ThreeDS2EventEffect(
    handleAction: (Context) -> Unit,
    viewEventFlow: Flow<ThreeDS2Event>,
) {
    val currentHandleAction by rememberUpdatedState(handleAction)
    val context = LocalContext.current

    LaunchedEffect(viewEventFlow) {
        viewEventFlow.collect { event ->
            when (event) {
                is ThreeDS2Event.HandleAction -> currentHandleAction(context)
            }
        }
    }
}
