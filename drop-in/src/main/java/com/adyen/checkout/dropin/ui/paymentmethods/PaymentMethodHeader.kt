/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 7/6/2021.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

import androidx.annotation.StringRes
import com.adyen.checkout.dropin.R

data class PaymentMethodHeader(
    val type: Int
) : PaymentMethodListItem {
    @StringRes
    val titleResId: Int = when (type) {
        TYPE_STORED_HEADER -> R.string.store_payment_methods_header
        TYPE_REGULAR_HEADER_WITH_STORED -> R.string.other_payment_methods
        TYPE_REGULAR_HEADER_WITHOUT_STORED -> R.string.payment_methods_header
        TYPE_GIFT_CARD_HEADER -> R.string.checkout_giftcard_payment_methods_header
        else -> R.string.payment_methods_header
    }

    @StringRes
    val actionResId: Int? = when (type) {
        TYPE_GIFT_CARD_HEADER -> R.string.checkout_giftcard_remove_button
        else -> null
    }

    override fun getViewType(): Int = PaymentMethodListItem.PAYMENT_METHODS_HEADER

    companion object {
        const val TYPE_STORED_HEADER = 0
        const val TYPE_REGULAR_HEADER_WITH_STORED = 1
        const val TYPE_REGULAR_HEADER_WITHOUT_STORED = 2
        const val TYPE_GIFT_CARD_HEADER = 3
    }
}
