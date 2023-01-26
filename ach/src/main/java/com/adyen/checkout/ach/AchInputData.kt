/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 24/1/2023.
 */

package com.adyen.checkout.ach

import com.adyen.checkout.components.base.InputData
import com.adyen.checkout.components.ui.AddressInputModel

data class AchInputData(
    var bankAccountNumber: String = "",
    var bankLocationId: String = "",
    var ownerName: String = "",
    var address: AddressInputModel = AddressInputModel(),
) : InputData
