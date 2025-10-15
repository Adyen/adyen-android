/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.CardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

internal data class CardPaymentComponentState(
    override val data: PaymentComponentData<CardPaymentMethod>,
    override val isValid: Boolean,
) : PaymentComponentState<CardPaymentMethod>
