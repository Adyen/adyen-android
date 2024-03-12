/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/2/2024.
 */

package com.adyen.checkout.example.data.storage

import androidx.annotation.Keep

@Keep
enum class ThreeDSMode {
    PREFER_NATIVE,
    REDIRECT,
    DISABLED,
}
