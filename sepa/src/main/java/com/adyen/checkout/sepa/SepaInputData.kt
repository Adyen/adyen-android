/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.sepa

import com.adyen.checkout.components.base.InputData

data class SepaInputData(
    var name: String = "",
    var iban: String = "",
) : InputData
