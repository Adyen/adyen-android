/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2023.
 */

package com.adyen.checkout.blik.old

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.BlikPaymentMethod

/**
 * Represents the state of [BlikComponent].
 */
data class BlikComponentState(
    override val data: PaymentComponentData<BlikPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<BlikPaymentMethod>
