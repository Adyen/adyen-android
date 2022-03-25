/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/9/2019.
 */
package com.adyen.checkout.card.data

data class ExpiryDate(
    val expiryMonth: Int,
    val expiryYear: Int,
) {

    companion object {
        @JvmField
        val EMPTY_DATE = ExpiryDate(0, 0)
        @JvmField
        val INVALID_DATE = ExpiryDate(-1, -1)
    }
}
