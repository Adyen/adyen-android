/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/2/2023.
 */

package com.adyen.checkout.upi

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.components.core.internal.ui.model.Validation

class UpiOutputData(virtualPaymentAddress: String) : OutputData {

    val virtualPaymentAddressFieldState = validateVirtualPaymentAddress(virtualPaymentAddress)

    override val isValid: Boolean
        get() = virtualPaymentAddressFieldState.validation.isValid()

    private fun validateVirtualPaymentAddress(virtualPaymentAddress: String): FieldState<String> =
        // TODO: validate VPA
        if (virtualPaymentAddress.isNotEmpty()) {
            FieldState(virtualPaymentAddress, Validation.Valid)
        } else {
            // TODO: use proper string resource
            FieldState(virtualPaymentAddress, Validation.Invalid(0))
        }
}
