/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 8/8/2023.
 */

package com.adyen.checkout.example.data.storage

import androidx.annotation.Keep

@Keep
enum class CardAddressMode {
    NONE,
    POSTAL_CODE,
    FULL_ADDRESS,
    LOOKUP,
}
