/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2023.
 */

package com.adyen.checkout.upi.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.upi.R

internal class UPIOutputData(
    val mode: UPIMode,
    virtualPaymentAddress: String,
) : OutputData {

    val virtualPaymentAddressFieldState = validateVirtualPaymentAddress(virtualPaymentAddress)

    override val isValid: Boolean
        get() = when (mode) {
            UPIMode.VPA -> virtualPaymentAddressFieldState.validation.isValid()
            UPIMode.QR -> true
        }

    private fun validateVirtualPaymentAddress(virtualPaymentAddress: String): FieldState<String> =
        if (virtualPaymentAddress.isNotBlank()) {
            FieldState(virtualPaymentAddress, Validation.Valid)
        } else {
            FieldState(virtualPaymentAddress, Validation.Invalid(R.string.checkout_upi_vpa_validation))
        }
}
