/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/10/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.ui.model.ExpiryDate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmField
val EMPTY_DATE: ExpiryDate = ExpiryDate(0, 0)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmField
val INVALID_DATE: ExpiryDate = ExpiryDate(-1, -1)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun toMMyyString(expiryMonth: String, expiryYear: String): String {
    val monthDigits = expiryMonth.padStart(2, '0')
    val yearDigits = expiryYear.takeLast(2).padStart(2, '0')
    return "$monthDigits/$yearDigits"
}
