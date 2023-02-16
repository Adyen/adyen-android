/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 16/2/2023.
 */

package com.adyen.checkout.ach.internal.ui.model

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.AddressFormUIState
import com.adyen.checkout.components.ui.AddressOutputData
import com.adyen.checkout.components.ui.FieldState

internal data class ACHDirectDebitOutputData(
    var bankAccountNumber: FieldState<String>,
    var bankLocationId: FieldState<String>,
    var ownerName: FieldState<String>,
    val addressState: AddressOutputData,
    val addressUIState: AddressFormUIState
) : OutputData {

    override val isValid: Boolean
        get() =
            bankAccountNumber.validation.isValid() &&
                bankLocationId.validation.isValid() &&
                ownerName.validation.isValid() &&
                addressState.isValid
}
