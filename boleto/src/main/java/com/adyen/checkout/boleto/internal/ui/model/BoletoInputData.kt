/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class BoletoInputData(
    var firstName: String = "",
    var lastName: String = "",
    var socialSecurityNumber: String = "",
    var address: AddressInputModel = AddressInputModel(),
    var isSendEmailSelected: Boolean = false,
    var shopperEmail: String = ""
) : InputData
