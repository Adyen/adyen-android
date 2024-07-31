/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2024.
 */

package com.adyen.checkout.twint

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.TwintPaymentMethod

/**
 * Represents the state of [TwintComponentState]
 */
data class TwintComponentState(
    override val data: PaymentComponentData<TwintPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
) : PaymentComponentState<TwintPaymentMethod>
