/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui

import com.adyen.checkout.core.data.PaymentComponentData
import com.adyen.checkout.core.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.core.paymentmethod.PaymentComponentState

/**
 * Represents the state of [MBWayComponent].
 */
internal data class MBWayComponentState(
    override val data: PaymentComponentData<MBWayPaymentMethod>,
) : PaymentComponentState<MBWayPaymentMethod>
