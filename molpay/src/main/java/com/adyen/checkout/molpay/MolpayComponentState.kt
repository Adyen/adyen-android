/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2023.
 */

package com.adyen.checkout.molpay

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.MolpayPaymentMethod

data class MolpayComponentState(
    override val data: PaymentComponentData<MolpayPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<MolpayPaymentMethod>
