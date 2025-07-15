/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.ui.core.old.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.old.internal.ui.model.AddressOutputData

internal data class BoletoOutputData(
    val firstNameState: FieldState<String>,
    val lastNameState: FieldState<String>,
    val socialSecurityNumberState: FieldState<String>,
    val addressState: AddressOutputData,
    val addressUIState: AddressFormUIState,
    val isEmailVisible: Boolean,
    val isSendEmailSelected: Boolean,
    val shopperEmailState: FieldState<String>
) : OutputData {

    override val isValid: Boolean
        get() = firstNameState.validation.isValid() &&
            lastNameState.validation.isValid() &&
            socialSecurityNumberState.validation.isValid() &&
            addressState.isValid &&
            shopperEmailState.validation.isValid()
}
