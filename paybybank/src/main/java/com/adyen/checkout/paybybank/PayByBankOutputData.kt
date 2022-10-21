/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.issuerlist.IssuerModel

class PayByBankOutputData(
    val selectedIssuer: IssuerModel?
): OutputData {
    // TODO validation
    override val isValid: Boolean = selectedIssuer != null
}
