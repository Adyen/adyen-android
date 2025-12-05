/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element

import com.adyen.checkout.ui.internal.theme.InternalColors

internal object SwitchDefaults {

    fun switchStyle(
        colors: InternalColors,
    ): InternalSwitchStyle {
        return InternalSwitchStyle(
            checkedHandleColor = colors.background,
            checkedTrackColor = colors.primary,
            uncheckedHandleColor = colors.primary,
            uncheckedTrackColor = colors.background,
        )
    }
}
