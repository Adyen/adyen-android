/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/6/2022.
 */

package com.adyen.checkout.econtext

import com.adyen.checkout.components.base.InputData

data class EContextInputData(
    var firstName: String = "",
    var lastName: String = "",
    var countryCode: String = "",
    var mobileNumber: String = "",
    var emailAddress: String = ""
) : InputData
