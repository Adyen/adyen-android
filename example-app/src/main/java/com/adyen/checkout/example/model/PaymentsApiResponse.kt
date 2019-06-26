/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */

package com.adyen.checkout.example.model

import com.adyen.checkout.base.model.paymentmethods.InputDetail
import com.adyen.checkout.base.model.payments.response.Action

data class PaymentsApiResponse(
    val resultCode: String? = null,
    val paymentData: String? = null,
    val details: List<InputDetail>? = null,
    val action: Action? = null
)
