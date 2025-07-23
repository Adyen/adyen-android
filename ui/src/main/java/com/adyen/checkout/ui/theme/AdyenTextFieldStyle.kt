/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable

// TODO - Add KDocs
@Immutable
data class AdyenTextFieldStyle(
    val backgroundColor: CheckoutColor? = null,
    val textColor: CheckoutColor? = null,
    val activeColor: CheckoutColor? = null,
    val errorColor: CheckoutColor? = null,
    val cornerRadius: Int? = null,
    val borderColor: CheckoutColor? = null,
    val borderWidth: Int? = null,
)
