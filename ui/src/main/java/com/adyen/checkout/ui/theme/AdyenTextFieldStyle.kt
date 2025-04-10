/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

data class AdyenTextFieldStyle(
    val type: Type = Type.OUTLINED,
    val backgroundColor: AdyenColor? = null,
    val textColor: AdyenColor? = null,
    val activeColor: AdyenColor? = null,
    val errorColor: AdyenColor? = null,
    val cornerRadius: Int? = null,
    val borderColor: AdyenColor? = null,
    val borderWidth: Int? = null,
) {

    enum class Type {
        OUTLINED,
        UNDERLINED,
    }
}
