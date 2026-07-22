/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/7/2026.
 */

package com.adyen.checkout.core.components.internal

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString
import com.adyen.checkout.ui.internal.theme.Dimensions
import com.adyen.checkout.ui.theme.CheckoutTheme

@Composable
internal fun CheckoutFullScreenDialog(
    theme: CheckoutTheme,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequest,
    ) {
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                // remove dark scrim
                window.setDimAmount(0f)
                // truly full window
                window.setLayout(MATCH_PARENT, MATCH_PARENT)
                // draw edge-to-edge
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(theme.colors.background.value),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // keep content out from under the bars, while the surface stays edge-to-edge
                    .safeDrawingPadding(),
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, resolveString(CheckoutLocalizationKey.GENERAL_CLOSE))
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = Dimensions.Spacing.Large, vertical = Dimensions.Spacing.Small)
                        .verticalScroll(rememberScrollState()),
                ) {
                    content()
                }
            }
        }
    }
}
