/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.button

import com.adyen.checkout.ui.internal.theme.InternalColors

internal object CheckoutButtonGroupDefaults {

    fun buttonGroupStyle(
        colors: InternalColors,
    ): InternalButtonGroupStyle {
        return InternalButtonGroupStyle(
            checkedContainerColor = colors.primary,
            checkedTextColor = colors.textOnPrimary,
            uncheckedContainerColor = colors.container,
            uncheckedTextColor = colors.text,
            disabledContentColor = colors.textOnDisabled,
            disabledContainerColor = colors.disabled,
        )
    }
}
