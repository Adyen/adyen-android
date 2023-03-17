/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/12/2020.
 */
package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData

internal data class MBWayOutputData(
    val phoneNumber: FieldState<String>
) : OutputData {

    override val isValid: Boolean
        get() = phoneNumber.validation.isValid()
}
