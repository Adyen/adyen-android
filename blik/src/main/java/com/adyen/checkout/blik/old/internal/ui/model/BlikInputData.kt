/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.blik.old.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class BlikInputData(
    var blikCode: String = "",
) : InputData
