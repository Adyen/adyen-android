/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 26/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod

// TODO: 24/11/2020 delete this class
class PaymentMethodsModel {
    var storedPaymentMethods: MutableList<PaymentMethod> = mutableListOf()
    var paymentMethods: MutableList<PaymentMethod> = mutableListOf()
}
