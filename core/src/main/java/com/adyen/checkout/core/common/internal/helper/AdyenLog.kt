/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/2/2024.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogger.LogLevel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun Any.adyenLog(
    level: LogLevel,
    throwable: Throwable? = null,
    log: () -> String,
) {
    if (AdyenLogDispatcher.isLoggable(level)) {
        AdyenLogDispatcher.write(level, adyenLogTag(this), log(), throwable)
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun adyenLogTag(source: Any): String {
    val fullClassName = source.javaClass.name
    val outerClassName = fullClassName.substringBefore('$').substringAfterLast('.')
    return "CO." + if (outerClassName.isEmpty()) {
        fullClassName
    } else {
        outerClassName.removeSuffix("Kt")
    }
}

/**
 * This is only meant for top level function where we cannot access `this`.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun adyenLog(
    level: LogLevel,
    tag: String,
    throwable: Throwable? = null,
    log: () -> String,
) {
    if (AdyenLogDispatcher.isLoggable(level)) {
        AdyenLogDispatcher.write(level, "CO.$tag", log(), throwable)
    }
}
