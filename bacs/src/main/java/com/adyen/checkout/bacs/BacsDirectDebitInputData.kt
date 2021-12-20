/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.base.InputData

data class BacsDirectDebitInputData(
    var holderName: String = "",
    var bankAccountNumber: String = "",
    var sortCode: String = "",
    var shopperEmail: String = "",
    var isAmountConsentChecked: Boolean = false,
    var isAccountConsentChecked: Boolean = false,
    var mode: BacsDirectDebitMode = BacsDirectDebitMode.INPUT
) : InputData
