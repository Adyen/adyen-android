/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/2/2024.
 */

package com.adyen.checkout.core.internal

import android.content.Context
import androidx.startup.Initializer

@Suppress("unused")
internal class AdyenCheckoutInitializer : Initializer<AdyenCheckoutInitializerType> {

    override fun create(context: Context): AdyenCheckoutInitializerType {
        ApplicationContextProvider.initialize(context.applicationContext)
        return object : AdyenCheckoutInitializerType {}
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

internal interface AdyenCheckoutInitializerType
