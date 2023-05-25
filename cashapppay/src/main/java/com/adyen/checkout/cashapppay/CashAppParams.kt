/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 28/3/2023.
 */

package com.adyen.checkout.cashapppay

internal data class CashAppParams(
    val clientId: String,
    val scopeId: String,
    val returnUrl: String,
)
