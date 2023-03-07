/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs.internal.ui.model

import com.adyen.checkout.bacs.BacsDirectDebitMode
import com.adyen.checkout.components.core.internal.ui.model.InputData

internal data class BacsDirectDebitInputData(
    var holderName: String = "",
    var bankAccountNumber: String = "",
    var sortCode: String = "",
    var shopperEmail: String = "",
    var isAmountConsentChecked: Boolean = false,
    var isAccountConsentChecked: Boolean = false,
    var mode: BacsDirectDebitMode = BacsDirectDebitMode.INPUT
) : InputData
