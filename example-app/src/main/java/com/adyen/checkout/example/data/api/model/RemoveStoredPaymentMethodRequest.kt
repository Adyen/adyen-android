/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/3/2022.
 */

package com.adyen.checkout.example.data.api.model

data class RemoveStoredPaymentMethodRequest(
    val recurringDetailReference: String,
    val merchantAccount: String,
    val shopperReference: String
)
