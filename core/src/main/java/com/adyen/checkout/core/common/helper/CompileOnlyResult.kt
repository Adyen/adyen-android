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

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Available<R>(val value: R) : CompileOnlyResult<R>

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data object Unavailable : CompileOnlyResult<Nothing>

    // Warning: do not call getOrNull() if R is a type from a compileOnly module that might not be on the
    // classpath. The JVM will need to resolve R for the checkcast, which happens outside the runCompileOnly
    // try-catch and will throw NoClassDefFoundError.
    fun getOrNull(): R? = when (this) {
        is Available -> value
        is Unavailable -> null
    }
}
