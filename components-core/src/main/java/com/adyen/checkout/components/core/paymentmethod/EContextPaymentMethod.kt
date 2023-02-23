/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/6/2022.
 */

package com.adyen.checkout.components.core.paymentmethod

abstract class EContextPaymentMethod : PaymentMethodDetails() {

    abstract var firstName: String?
    abstract var lastName: String?
    abstract var telephoneNumber: String?
    abstract var shopperEmail: String?

    companion object {
        const val FIRST_NAME = "firstName"
        const val LAST_NAME = "lastName"
        const val TELEPHONE_NUMBER = "telephoneNumber"
        const val SHOPPER_EMAIL = "shopperEmail"
    }
}
