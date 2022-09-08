/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */
package com.adyen.checkout.issuerlist

import com.adyen.checkout.components.base.OutputData

class IssuerListOutputData(
    val selectedIssuer: IssuerModel?,
) : OutputData {

    override val isValid = selectedIssuer != null
}
