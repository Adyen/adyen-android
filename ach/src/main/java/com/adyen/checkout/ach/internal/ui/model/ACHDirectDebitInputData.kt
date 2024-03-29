/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class ACHDirectDebitInputData(
    var bankAccountNumber: String = "",
    var bankLocationId: String = "",
    var ownerName: String = "",
    var address: AddressInputModel = AddressInputModel(),
    var isStorePaymentMethodSwitchChecked: Boolean = false,
) : InputData
