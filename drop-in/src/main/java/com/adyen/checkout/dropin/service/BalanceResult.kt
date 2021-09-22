/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2021.
 */

package com.adyen.checkout.dropin.service

data class BalanceResult(
    val balance: String,
    val transactionLimit: String?
)
