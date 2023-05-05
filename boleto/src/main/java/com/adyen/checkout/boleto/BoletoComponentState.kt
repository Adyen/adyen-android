/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.GenericPaymentMethod

/**
 * Represents the state of [BoletoComponent].
 */
data class BoletoComponentState(
    override val data: PaymentComponentData<GenericPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
) : PaymentComponentState<GenericPaymentMethod>
