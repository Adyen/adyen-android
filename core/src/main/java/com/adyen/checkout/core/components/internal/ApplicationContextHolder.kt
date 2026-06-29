/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.components.CheckoutController

/**
 * Holds a reference to the application [Context] so internal SDK code can read app-level data
 * (e.g. analytics setup) without requiring merchants to pass a [Context] when constructing
 * a [CheckoutController]. The reference is captured at app startup by [CheckoutCoreInitializer]
 * via `androidx.startup`.
 *
 * Storing the application context (process-lifetime) in a singleton is the standard Android
 * pattern used by `androidx.startup`, WorkManager, Firebase, etc. and does not leak.
 */
@SuppressLint("StaticFieldLeak")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ApplicationContextHolder {

    @Volatile
    private var applicationContext: Context? = null

    /**
     * Stores the application context. Always coerces to [Context.getApplicationContext] to avoid
     * accidentally retaining an Activity or Service context.
     */
    fun set(context: Context) {
        applicationContext = context.applicationContext
    }

    /**
     * Returns the cached application context.
     *
     * @throws IllegalStateException if the context has not been initialized yet, which only
     * happens when `androidx.startup` is disabled by the host app.
     */
    fun require(): Context = applicationContext ?: error(
        "Adyen Checkout has not been initialized. This usually means androidx.startup was disabled in your app.",
    )

    @VisibleForTesting
    fun reset() {
        applicationContext = null
    }
}
