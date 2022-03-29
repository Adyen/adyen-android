/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/5/2021.
 */

package com.adyen.checkout.components

import android.app.Application
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

class AlwaysAvailablePaymentMethod : PaymentMethodAvailabilityCheck<Configuration> {

    override fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: Configuration?,
        callback: ComponentAvailableCallback<Configuration>
    ) {
        callback.onAvailabilityResult(true, paymentMethod, configuration)
    }
}
