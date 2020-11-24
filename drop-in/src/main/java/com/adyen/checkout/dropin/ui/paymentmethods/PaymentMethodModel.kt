/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/11/2020.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod

data class PaymentMethodModel(
    val type: String,
    val name: String,
    val paymentMethod: PaymentMethod = PaymentMethod()
)