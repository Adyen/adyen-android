/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.blik.internal.ui.properties

import com.adyen.checkout.ui.internal.element.input.TextFieldSeparator

/**
 * This field is formatted as such 123 456
 */
internal object BlikCodeProperties {
    const val BLIK_CODE_MAX_LENGTH = 6
    const val BLIK_CODE_SEPARATOR = ' '

    // e.g 123 456
    val BLIK_CODE_SEPARATORS = listOf(
        TextFieldSeparator(BLIK_CODE_SEPARATOR, 3),
    )
}
