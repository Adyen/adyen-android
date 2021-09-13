/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.components.base.InputData

data class GiftCardInputData(
    var cardNumber: String = "",
    var pin: String = ""
) : InputData
