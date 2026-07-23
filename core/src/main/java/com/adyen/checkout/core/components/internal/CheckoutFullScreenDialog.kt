/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/7/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    // TODO investigate switching to a full height ModalBottomSheet later - currently the animation looks janky
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(theme.colors.background.value))
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
