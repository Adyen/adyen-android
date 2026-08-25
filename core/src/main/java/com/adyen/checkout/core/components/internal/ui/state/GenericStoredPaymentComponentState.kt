/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.GenericStoredDetails
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

internal data class GenericStoredPaymentComponentState(
    override val data: PaymentComponentData<GenericStoredDetails>,
    override val isValid: Boolean,
) : PaymentComponentState<GenericStoredDetails>
