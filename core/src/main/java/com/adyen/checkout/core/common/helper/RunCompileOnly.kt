/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/5/2023.
 */

package com.adyen.checkout.core.common.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogger.LogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <R> runCompileOnly(block: () -> R): CompileOnlyResult<R> {
    try {
        return CompileOnlyResult.Available(block())
    } catch (e: ClassNotFoundException) {
        adyenLog(LogLevel.WARN, "runCompileOnly", e) { "Class not found. Are you missing a dependency?" }
    } catch (e: NoClassDefFoundError) {
        adyenLog(LogLevel.WARN, "runCompileOnly", e) { "Class not found. Are you missing a dependency?" }
    }

    return CompileOnlyResult.Unavailable
}
