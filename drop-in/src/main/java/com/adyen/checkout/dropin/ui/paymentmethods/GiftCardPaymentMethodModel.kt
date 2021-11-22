/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

data class GiftCardPaymentMethodModel(
    val imageId: String,
    val lastFour: String
) : PaymentMethodListItem {
    override fun getViewType(): Int = PaymentMethodListItem.GIFT_CARD_PAYMENT_METHOD
}
