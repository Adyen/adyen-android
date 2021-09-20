/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/11/2020.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

data class PaymentMethodModel(
    val index: Int,
    val type: String,
    val name: String,
    val icon: String,
    val drawIconBorder: Boolean
) : PaymentMethodListItem {
    override fun getViewType(): Int = PaymentMethodListItem.PAYMENT_METHOD
}
