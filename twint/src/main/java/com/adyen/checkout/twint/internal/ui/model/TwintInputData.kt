/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class TwintInputData(
    var isStorePaymentSelected: Boolean = false,
) : InputData
