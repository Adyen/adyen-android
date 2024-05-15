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

internal class UPIOutputData(
    val availableModes: List<UPIMode>,
    val selectedMode: UPISelectedMode,
    var selectedUPIIntentItem: UPIIntentItem? = null,
    val virtualPaymentAddressFieldState: FieldState<String>,
    val intentVirtualPaymentAddressFieldState: FieldState<String>,
) : OutputData {

    override val isValid: Boolean
        get() = when (selectedMode) {
            UPISelectedMode.INTENT -> {
                when (selectedUPIIntentItem) {
                    is UPIIntentItem.PaymentApp -> true
                    UPIIntentItem.GenericApp -> true
                    is UPIIntentItem.ManualInput -> intentVirtualPaymentAddressFieldState.validation.isValid()
                    null -> false
                }
            }

            UPISelectedMode.VPA -> virtualPaymentAddressFieldState.validation.isValid()
            UPISelectedMode.QR -> true
        }
}
