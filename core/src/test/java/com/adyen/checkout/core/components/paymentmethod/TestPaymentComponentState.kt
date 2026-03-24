/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.components.data.PaymentComponentData

class TestPaymentComponentState(
    override val data: PaymentComponentData<TestPaymentDetails> = PaymentComponentData(
        paymentMethod = TestPaymentDetails(),
        order = null,
    ),
    override val isValid: Boolean = true,
) : PaymentComponentState<TestPaymentDetails>
