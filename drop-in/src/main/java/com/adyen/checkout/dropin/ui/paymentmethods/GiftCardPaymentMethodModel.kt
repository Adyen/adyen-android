/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/11/2021.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import com.adyen.checkout.components.model.payments.Amount
import java.util.Locale

data class GiftCardPaymentMethodModel(
    val imageId: String,
    val lastFour: String,
    val amount: Amount?,
    val transactionLimit: Amount?,
    val shopperLocale: Locale?
) : PaymentMethodListItem {
    override fun getViewType(): Int = PaymentMethodListItem.GIFT_CARD_PAYMENT_METHOD
}
