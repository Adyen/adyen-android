/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2023.
 */

package com.adyen.checkout.instant

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails

data class InstantComponentState(
    override val data: PaymentComponentData<PaymentMethodDetails>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<PaymentMethodDetails>
