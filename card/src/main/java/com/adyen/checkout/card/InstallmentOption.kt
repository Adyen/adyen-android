/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/10/2021.
 */

package com.adyen.checkout.card

enum class InstallmentOption(val type: String?) {
    ONE_TIME(null),
    REGULAR("regular"),
    REVOLVING("revolving")
}
