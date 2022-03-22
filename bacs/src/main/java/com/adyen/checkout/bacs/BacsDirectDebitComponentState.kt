/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/11/2021.
 */

package com.adyen.checkout.bacs

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.BacsDirectDebitPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData

class BacsDirectDebitComponentState(
    paymentComponentData: PaymentComponentData<BacsDirectDebitPaymentMethod>,
    isInputValid: Boolean,
    isReady: Boolean,
    val mode: BacsDirectDebitMode = BacsDirectDebitMode.INPUT,
) : PaymentComponentState<BacsDirectDebitPaymentMethod>(paymentComponentData, isInputValid, isReady) {

    override val isValid: Boolean
        get() = super.isValid && mode == BacsDirectDebitMode.CONFIRMATION
}
