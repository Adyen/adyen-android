/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.GooglePayPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

internal class GooglePayPaymentComponentState(
    override val data: PaymentComponentData<GooglePayPaymentMethod>,
    override val isValid: Boolean,
) : PaymentComponentState<GooglePayPaymentMethod>
