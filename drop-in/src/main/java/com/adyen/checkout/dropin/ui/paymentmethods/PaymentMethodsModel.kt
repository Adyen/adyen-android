/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 26/7/2019.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod

class PaymentMethodsModel {
    var storedPaymentMethods: MutableList<PaymentMethod> = mutableListOf()
    var paymentMethods: MutableList<PaymentMethod> = mutableListOf()
}
