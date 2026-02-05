/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import com.adyen.checkout.core.components.internal.PaymentMethodProvider
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.googlepay.internal.ui.GooglePayFactory

@Keep
internal class GooglePayInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val factory = GooglePayFactory()
        PaymentMethodProvider.register(txVariant = PaymentMethodTypes.GOOGLE_PAY, factory = factory)
        PaymentMethodProvider.register(txVariant = PaymentMethodTypes.GOOGLE_PAY_LEGACY, factory = factory)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
