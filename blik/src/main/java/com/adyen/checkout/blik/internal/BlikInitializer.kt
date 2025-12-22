/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import com.adyen.checkout.blik.internal.ui.BlikFactory
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

@Keep
internal class BlikInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        PaymentMethodProvider.register(PaymentMethodTypes.BLIK, BlikFactory())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
