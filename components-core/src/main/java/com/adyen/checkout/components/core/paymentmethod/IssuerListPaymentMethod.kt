/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/5/2019.
 */
package com.adyen.checkout.components.core.paymentmethod

@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
abstract class IssuerListPaymentMethod : PaymentMethodDetails() {
    abstract var issuer: String?

    companion object {
        const val ISSUER = "issuer"
    }
}
