/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/6/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.GenericDetails
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

internal data class GenericPaymentComponentState(
    override val data: PaymentComponentData<GenericDetails>,
    override val isValid: Boolean,
) : PaymentComponentState<GenericDetails>
