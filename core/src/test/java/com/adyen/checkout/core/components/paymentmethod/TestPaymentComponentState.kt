/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.core.components.paymentmethod

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount

class TestPaymentComponentState(
    override val data: PaymentComponentData<TestPaymentMethod> = PaymentComponentData(
        paymentMethod = TestPaymentMethod(),
        order = null,
        amount = Amount(currency = "EUR"),
    ),
    override val isValid: Boolean = true,
) : PaymentComponentState<TestPaymentMethod>
