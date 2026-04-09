/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2025.
 */

package com.adyen.checkout.mbway.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.mbway.internal.ui.MBWayFactory

@Keep
internal class MBWayInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        PaymentMethodProvider.register(PaymentMethodTypes.MB_WAY, MBWayFactory())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
