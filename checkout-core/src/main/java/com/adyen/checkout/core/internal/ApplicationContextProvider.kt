/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/2/2024.
 */

package com.adyen.checkout.core.internal

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RestrictTo

@SuppressLint("StaticFieldLeak")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ApplicationContextProvider {

    private var isInitialized: Boolean = false

    lateinit var context: Context

    fun initialize(applicationContext: Context) {
        if (isInitialized) return

        context = applicationContext

        isInitialized = true
    }
}
