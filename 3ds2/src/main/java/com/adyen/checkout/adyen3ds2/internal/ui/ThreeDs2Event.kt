/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/10/2025.
 */

package com.adyen.checkout.adyen3ds2.internal.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow

@Suppress("TooGenericExceptionCaught")
@SuppressLint("ComposableNaming")
@Composable
internal fun threeDsEvent(
    handleAction: (Context) -> Unit,
    viewEventFlow: Flow<ThreeDS2Event>,
    onError: (RuntimeException) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(handleAction, viewEventFlow, onError) {
        viewEventFlow.collect { event ->
            when (event) {
                is ThreeDS2Event.HandleAction -> handleAction(context)
            }
        }
    }
}
