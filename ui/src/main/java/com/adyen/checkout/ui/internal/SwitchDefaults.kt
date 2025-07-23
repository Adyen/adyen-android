/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/5/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Composable
import com.adyen.checkout.ui.theme.CheckoutSwitchStyle

internal object SwitchDefaults {

    @Composable
    fun switchStyle(style: CheckoutSwitchStyle?): InternalSwitchStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalSwitchStyle(
            checkedHandleColor = style?.checkedHandleColor?.toCompose() ?: colors.background,
            checkedTrackColor = style?.checkedHandleColor?.toCompose() ?: colors.primary,
            uncheckedHandleColor = style?.checkedHandleColor?.toCompose() ?: colors.primary,
            uncheckedTrackColor = style?.checkedHandleColor?.toCompose() ?: colors.background,
        )
    }
}
