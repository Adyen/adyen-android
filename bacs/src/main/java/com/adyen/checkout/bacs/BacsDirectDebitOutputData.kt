/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState

data class BacsDirectDebitOutputData(
    val holderNameState: FieldState<String>,
    val bankAccountNumberState: FieldState<String>,
    val sortCodeState: FieldState<String>,
    val shopperEmailState: FieldState<String>,
    val isAmountConsentChecked: Boolean,
    val isAccountConsentChecked: Boolean,
    val mode: BacsDirectDebitMode,
) : OutputData {

    override val isValid: Boolean
        get() =
            holderNameState.validation.isValid() &&
                bankAccountNumberState.validation.isValid() &&
                sortCodeState.validation.isValid() &&
                shopperEmailState.validation.isValid() &&
                isAmountConsentChecked &&
                isAccountConsentChecked
}
