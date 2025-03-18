/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/1/2025.
 */

package com.adyen.checkout.payto

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PayToPaymentMethod

/**
 * Represents the state of [PayToComponent].
 */
data class PayToComponentState(
    override val data: PaymentComponentData<PayToPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<PayToPaymentMethod>
