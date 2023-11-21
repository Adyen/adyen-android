/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/11/2023.
 */

package com.adyen.checkout.components.core.internal.util

import androidx.annotation.RestrictTo
import java.text.NumberFormat
import java.util.Locale

/**
 * Format the [Int] to be displayed to the user based on the Locale.
 *
 * @param locale The locale the number will be formatted with.
 * @return A formatted string displaying value.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Int.format(locale: Locale): String = NumberFormat.getInstance(locale).format(this)
