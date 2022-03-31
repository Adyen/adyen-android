/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.data.api.model

data class AdditionalData(
    val allow3DS2: String = "false",
    val executeThreeD: String = "false"
)
