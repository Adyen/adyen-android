/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 24/8/2022.
 */

package com.adyen.checkout.onlinebankingcz

import com.adyen.checkout.issuerlist.IssuerListOutputData
import com.adyen.checkout.issuerlist.IssuerModel

class OnlineBankingCZOutputData(
    val termsAndConditionsLink: String,
    selectedIssuer: IssuerModel?
) : IssuerListOutputData(selectedIssuer) {
    override val isValid: Boolean
        get() = selectedIssuer != null
}
