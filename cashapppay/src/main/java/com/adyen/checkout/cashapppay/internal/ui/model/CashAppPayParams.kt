/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui.model

internal data class CashAppPayParams(
    val clientId: String,
    val scopeId: String,
    val returnUrl: String,
)
