/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import com.adyen.checkout.components.base.OutputData

class PayByBankOutputData: OutputData {
    // TODO validation
    override val isValid: Boolean
        get() = true
}
