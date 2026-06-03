/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/4/2025.
 */

package com.adyen.checkout.ui.internal.theme

import androidx.annotation.FontRes
import androidx.compose.runtime.Immutable

// This class is internal for now, but if we will support customizing this in the future, it should be public.
@Immutable
internal data class CheckoutTextStyles(
    @field:FontRes val font: Int?,
) {

    companion object {

        fun default(
            @FontRes font: Int? = null,
        ) = CheckoutTextStyles(
            font = font,
        )
    }
}
