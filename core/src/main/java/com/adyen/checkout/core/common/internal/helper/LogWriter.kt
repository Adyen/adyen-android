/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 11/11/2026.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogger.LogLevel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun interface LogWriter {
    fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?)
}
