/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/8/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.components.paymentmethod.StoredCardDetails

internal data class StoredCardPaymentComponentState(
    override val data: PaymentComponentData<StoredCardDetails>,
    override val isValid: Boolean,
) : PaymentComponentState<StoredCardDetails>
