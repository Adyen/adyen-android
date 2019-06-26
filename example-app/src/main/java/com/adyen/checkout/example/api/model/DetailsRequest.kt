/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 19/4/2019.
 */

package com.adyen.checkout.example.api.model

data class DetailsRequest(
    val paymentData: String?
    // TODO add typed details
)
