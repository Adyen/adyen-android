/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/3/2022.
 */
package com.adyen.checkout.example.data.api.model

data class ThreeDS2RequestDataRequest(
    val deviceChannel: String = "app",
    val challengeIndicator: String = "requestChallenge"
)
