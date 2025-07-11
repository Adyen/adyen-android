/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.mbway.old.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class MBWayInputData(
    var countryCode: String = "",
    var localPhoneNumber: String = ""
) : InputData
