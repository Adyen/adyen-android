/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/11/2021.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.paymentmethod.BacsDirectDebitPaymentMethod

data class BacsDirectDebitComponentState(
    override val data: PaymentComponentData<BacsDirectDebitPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
    val mode: BacsDirectDebitMode = BacsDirectDebitMode.INPUT,
) : PaymentComponentState<BacsDirectDebitPaymentMethod> {

    override val isValid: Boolean
        get() = super.isValid && mode == BacsDirectDebitMode.CONFIRMATION
}
