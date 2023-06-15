/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/6/2023.
 */

package com.adyen.checkout.atome.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.InputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressInputModel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AtomeInputData(
    var firstName: String = "",
    var lastName: String = "",
    var countryCode: String = "",
    var mobileNumber: String = "",
    var billingAddress: AddressInputModel = AddressInputModel(),
    var shippingAddress: AddressInputModel = AddressInputModel(),
) : InputData
