/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/7/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider

@Composable
internal fun CheckoutFullScreenDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect(window) {
            // Remove the dim to make the transition look better
            window?.setDimAmount(0f)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = CheckoutThemeProvider.colors.background)
                // keep content out from under the bars, while the surface stays edge-to-edge
                .safeDrawingPadding(),
        ) {
            content()
        }
    }
}
