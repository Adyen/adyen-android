/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2023.
 */

package com.adyen.checkout.twint

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.GenericPaymentMethod

data class TwintComponentState(
    override val data: PaymentComponentData<GenericPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
) : PaymentComponentState<GenericPaymentMethod>
