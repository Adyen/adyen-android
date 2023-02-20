/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/2/2023.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails

internal class TestComponentState(
    override val data: PaymentComponentData<PaymentMethodDetails>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<PaymentMethodDetails>
