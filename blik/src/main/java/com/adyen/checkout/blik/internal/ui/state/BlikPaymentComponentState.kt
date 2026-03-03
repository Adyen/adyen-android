/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.blik.internal.ui.BlikComponent
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.BlikDetails
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

/**
 * Represents the state of [BlikComponent].
 */
internal data class BlikPaymentComponentState(
    override val data: PaymentComponentData<BlikDetails>,
    override val isValid: Boolean,
) : PaymentComponentState<BlikDetails>
