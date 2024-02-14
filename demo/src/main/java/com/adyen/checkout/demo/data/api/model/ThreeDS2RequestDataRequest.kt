/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/2/2024.
 */

package com.adyen.checkout.demo.data.api.model

import androidx.annotation.Keep

@Keep
data class ThreeDS2RequestDataRequest(
    val deviceChannel: String = "app",
    val challengeIndicator: String = "requestChallenge"
)
