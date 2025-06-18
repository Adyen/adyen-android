/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/2/2024.
 */

package com.adyen.checkout.core.old.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.AdyenLogger

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun Any.adyenLog(
    level: AdyenLogLevel,
    throwable: Throwable? = null,
    log: () -> String,
) {
    if (AdyenLogger.logger.shouldLog(level)) {
        val fullClassName = this::class.java.name
        val outerClassName = fullClassName.substringBefore('$').substringAfterLast('.')
        val tag = "CO." + if (outerClassName.isEmpty()) {
            fullClassName
        } else {
            outerClassName.removeSuffix("Kt")
        }

        AdyenLogger.logger.log(level, tag, log(), throwable)
    }
}

/**
 * This is only meant for top level function where we cannot access `this`.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun adyenLog(
    level: AdyenLogLevel,
    tag: String,
    throwable: Throwable? = null,
    log: () -> String,
) {
    if (AdyenLogger.logger.shouldLog(level)) {
        AdyenLogger.logger.log(level, "CO.$tag", log(), throwable)
    }
}
