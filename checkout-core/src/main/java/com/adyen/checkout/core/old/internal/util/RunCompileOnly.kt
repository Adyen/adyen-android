/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/5/2023.
 */

package com.adyen.checkout.core.old.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.AdyenLogLevel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
inline fun <R : Any> runCompileOnly(block: () -> R): R? {
    try {
        return block()
    } catch (e: ClassNotFoundException) {
        adyenLog(AdyenLogLevel.WARN, "runCompileOnly", e) { "Class not found. Are you missing a dependency?" }
    } catch (e: NoClassDefFoundError) {
        adyenLog(AdyenLogLevel.WARN, "runCompileOnly", e) { "Class not found. Are you missing a dependency?" }
    }

    return null
}
