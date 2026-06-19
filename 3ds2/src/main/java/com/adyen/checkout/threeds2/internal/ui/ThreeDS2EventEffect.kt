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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.flow.Flow

@Composable
internal fun ThreeDS2EventEffect(
    handleAction: (Context, UiCustomization) -> Unit,
    viewEventFlow: Flow<ThreeDS2Event>,
    onError: (InternalCheckoutError) -> Unit,
) {
    val context = LocalContext.current
    val colors = CheckoutThemeProvider.colors
    val attributes = CheckoutThemeProvider.attributes
    val uiCustomization = remember(colors, attributes) { mapToUiCustomization(colors, attributes) }
    LaunchedEffect(handleAction, viewEventFlow, onError, uiCustomization) {
        viewEventFlow.collect { event ->
            when (event) {
                is ThreeDS2Event.HandleAction -> handleAction(context, uiCustomization)
            }
        }
    }
}
