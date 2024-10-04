/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/10/2024.
 */
package com.adyen.checkout.core.ui.model

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
