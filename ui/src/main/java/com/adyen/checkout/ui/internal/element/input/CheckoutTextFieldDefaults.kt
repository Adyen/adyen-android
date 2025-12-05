/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.element.input

import com.adyen.checkout.ui.internal.theme.InternalAttributes
import com.adyen.checkout.ui.internal.theme.InternalColors

internal object CheckoutTextFieldDefaults {

    fun textFieldStyle(
        colors: InternalColors,
        attributes: InternalAttributes,
    ): InternalTextFieldStyle {
        return InternalTextFieldStyle(
            backgroundColor = colors.container,
            textColor = colors.text,
            activeColor = colors.primary,
            errorColor = colors.destructive,
            cornerRadius = attributes.cornerRadius,
            borderColor = colors.containerOutline,
            borderWidth = 1,
        )
    }
}
