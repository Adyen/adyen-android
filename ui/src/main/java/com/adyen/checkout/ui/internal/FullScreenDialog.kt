/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/6/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

/**
 * A full-screen dialog that adjusts its system UI appearance based on the background color of the current theme and
 * applies the correct insets based on the system bars.
 *
 * @param onDismissRequest A lambda to be invoked when the user requests to dismiss the dialog, for example, by
 * pressing the back button.
 * @param content The composable content to be displayed inside the dialog.
 */
@Suppress("MagicNumber")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun FullScreenDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        val isBackgroundColorLight = CheckoutThemeProvider.colors.background.luminance() > 0.5
        (LocalView.current.parent as? DialogWindowProvider)?.window?.let {
            WindowCompat.getInsetsController(it, it.decorView)
                .isAppearanceLightStatusBars = isBackgroundColorLight
        }

        Surface(
            color = CheckoutThemeProvider.colors.background,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(Modifier.systemBarsPadding()) {
                content()
            }
        }
    }
}
