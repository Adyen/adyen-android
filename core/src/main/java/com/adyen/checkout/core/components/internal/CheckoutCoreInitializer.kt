/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer

@Keep
internal class CheckoutCoreInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        ApplicationContextHolder.set(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
