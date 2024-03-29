/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/5/2021.
 */
package com.adyen.checkout.components.core.internal

import android.app.Application
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod

/**
 * Specifies whether a certain payment method is available for use with the provided parameters.
 *
 * @param ConfigurationT The Configuration for the Component corresponding to this payment method. Simply use
 * [Configuration] if not applicable.
 */
interface PaymentMethodAvailabilityCheck<ConfigurationT : Configuration> {

    fun isAvailable(
        applicationContext: Application,
        paymentMethod: PaymentMethod,
        configuration: ConfigurationT?,
        callback: ComponentAvailableCallback
    )

    fun isAvailable(
        application: Application,
        paymentMethod: PaymentMethod,
        checkoutConfiguration: CheckoutConfiguration,
        callback: ComponentAvailableCallback
    )
}
