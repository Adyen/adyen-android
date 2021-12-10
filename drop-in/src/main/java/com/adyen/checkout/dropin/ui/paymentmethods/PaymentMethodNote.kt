/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 26/11/2021.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

data class PaymentMethodNote(val note: String) : PaymentMethodListItem {

    override fun getViewType(): Int = PaymentMethodListItem.PAYMENT_METHODS_NOTE
}
