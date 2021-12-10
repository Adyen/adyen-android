/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

interface PaymentMethodListItem {

    fun getViewType(): Int

    companion object {
        // View types
        const val PAYMENT_METHODS_HEADER = 1
        const val STORED_PAYMENT_METHOD = 2
        const val PAYMENT_METHOD = 3
        const val GIFT_CARD_PAYMENT_METHOD = 4
        const val PAYMENT_METHODS_NOTE = 5
    }
}
