/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel

internal data class PayByBankOutputData(
    val selectedIssuer: IssuerModel?,
    val issuers: List<IssuerModel>,
) : OutputData {
    override val isValid: Boolean = selectedIssuer != null
}
