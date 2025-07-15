/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.model

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CountryModel(
    val isoCode: String,
    val countryName: String,
    val callingCode: String,
) {
    fun toShortString(): String {
        return "$isoCode $callingCode"
    }
}
