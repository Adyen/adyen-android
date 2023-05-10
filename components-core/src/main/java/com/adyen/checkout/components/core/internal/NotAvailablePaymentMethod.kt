/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/5/2023.
 */

package com.adyen.checkout.components.core.internal

import android.app.Application
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class NotAvailablePaymentMethod : PaymentMethodAvailabilityCheck<Configuration> {

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: Configuration?,
        callback: ComponentAvailableCallback
    ) {
        callback.onAvailabilityResult(false, paymentMethod)
    }
}
