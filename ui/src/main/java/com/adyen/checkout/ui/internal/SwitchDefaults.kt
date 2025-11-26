/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/5/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.compose.runtime.Composable
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider

internal object SwitchDefaults {

    @Composable
    fun switchStyle(): InternalSwitchStyle {
        val colors = CheckoutThemeProvider.colors
        return InternalSwitchStyle(
            checkedHandleColor = colors.background,
            checkedTrackColor = colors.primary,
            uncheckedHandleColor = colors.primary,
            uncheckedTrackColor = colors.background,
        )
    }
}
