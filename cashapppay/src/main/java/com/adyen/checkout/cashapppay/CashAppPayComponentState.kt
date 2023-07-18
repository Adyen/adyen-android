/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.CashAppPayPaymentMethod

/**
 * Represents the state of [CashAppPayComponent]
 */
data class CashAppPayComponentState(
    override val data: PaymentComponentData<CashAppPayPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
) : PaymentComponentState<CashAppPayPaymentMethod>
