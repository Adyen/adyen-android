/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/2/2023.
 */

package com.adyen.checkout.ui.core.old

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails

internal data class TestComponentState(
    override val data: PaymentComponentData<PaymentMethodDetails>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<PaymentMethodDetails>
