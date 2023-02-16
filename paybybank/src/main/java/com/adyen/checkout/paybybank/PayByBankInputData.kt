/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import com.adyen.checkout.components.base.InputData
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel

class PayByBankInputData(
    var query: String? = null,
    var selectedIssuer: IssuerModel? = null
) : InputData
