/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/11/2024.
 */

package com.adyen.checkout.paybybankus.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem

internal data class PayByBankUSOutputData(
    val brandList: List<LogoTextItem>
) : OutputData {
    override val isValid: Boolean = true
}
