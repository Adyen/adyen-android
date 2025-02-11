/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/2/2025.
 */

package com.adyen.checkout.payto.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class PayToInputData(
    var mode: PayToMode = PayToMode.PAY_ID,
    var payIdTypeModel: PayIdTypeModel? = null,
) : InputData
