/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/3/2022.
 */

package com.adyen.checkout.example.data.api.model

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
data class BalanceRequest(
    val paymentMethod: JSONObject,
    val amount: JSONObject,
    val merchantAccount: String
)
