/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/6/2022.
 */

package com.adyen.checkout.econtext

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState

data class EContextOutputData(
    val firstNameState: FieldState<String>,
    val lastNameState: FieldState<String>,
    val phoneNumberState: FieldState<String>,
    val emailAddressState: FieldState<String>
) : OutputData {
    override val isValid: Boolean
        get() = firstNameState.validation.isValid() &&
            lastNameState.validation.isValid() &&
            phoneNumberState.validation.isValid() &&
            emailAddressState.validation.isValid()
}
