/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/12/2023.
 */

package com.adyen.checkout.googlepay.old

/**
 * Class containing some of the parameters required to initialize the Google Pay button.
 */
data class GooglePayButtonParameters(
    val allowedPaymentMethods: String,
)
