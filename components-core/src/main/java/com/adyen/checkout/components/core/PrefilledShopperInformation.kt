/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by gemini-code-assist on 23/6/2026.
 */

package com.adyen.checkout.components.core

import android.os.Parcelable
import androidx.annotation.RestrictTo
import kotlinx.parcelize.Parcelize

@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
@Parcelize
data class PrefilledShopperInformation(
    val card: CardInformation? = null,
) : Parcelable

@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
@Parcelize
data class CardInformation(
    val holderName: String? = null,
) : Parcelable
