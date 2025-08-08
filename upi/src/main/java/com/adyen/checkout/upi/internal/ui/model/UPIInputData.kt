/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2023.
 */

package com.adyen.checkout.upi.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class UPIInputData(
    var selectedMode: UPISelectedMode? = null,
    var selectedUPIIntentItem: UPIIntentItem? = null,
    var vpaVirtualPaymentAddress: String = "",
) : InputData
