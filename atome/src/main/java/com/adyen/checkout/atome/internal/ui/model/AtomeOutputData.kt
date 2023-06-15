/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/6/2023.
 */

package com.adyen.checkout.atome.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData

class AtomeOutputData(
    val firstNameState: FieldState<String>,
    val lastNameState: FieldState<String>,
    val phoneNumberState: FieldState<String>,
    val billingAddressState: AddressOutputData,
) : OutputData {
    override val isValid: Boolean
        get() = firstNameState.validation.isValid() &&
            lastNameState.validation.isValid() &&
            phoneNumberState.validation.isValid() &&
            billingAddressState.isValid
}
