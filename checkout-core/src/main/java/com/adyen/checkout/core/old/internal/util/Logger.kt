/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.old.internal.util

import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.RestrictTo

/**
 * Log manager for Checkout.
 * Serves as a proxy managing what and how to log information.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object Logger {

    @IntDef(SENSITIVE, Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, NONE)
    @Retention(AnnotationRetention.SOURCE)
    @Deprecated("Deprecated", ReplaceWith("AdyenLogLevel", "com.adyen.checkout.core.AdyenLogLevel"))
    annotation class LogLevel

    private const val SENSITIVE = -1

    @Deprecated("Deprecated", ReplaceWith("AdyenLogLevel.NONE", "com.adyen.checkout.core.AdyenLogLevel"))
    const val NONE = Log.ASSERT + 1
}
