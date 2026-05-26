/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/5/2026.
 */

package com.adyen.checkout.core.common.helper

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed interface CompileOnlyResult<out R> {
    data class Available<R>(val value: R) : CompileOnlyResult<R>
    data object Unavailable : CompileOnlyResult<Nothing>

    fun getOrNull(): R? = when (this) {
        is Available -> value
        is Unavailable -> null
    }
}
