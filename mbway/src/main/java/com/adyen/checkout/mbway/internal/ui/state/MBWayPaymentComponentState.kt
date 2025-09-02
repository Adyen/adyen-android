/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.MBWayPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.mbway.internal.ui.MBWayComponent

/**
 * Represents the state of [MBWayComponent].
 */
// TODO - check if we need to make this public
internal data class MBWayPaymentComponentState(
    override val data: PaymentComponentData<MBWayPaymentMethod>,
    override val isValid: Boolean,
) : PaymentComponentState<MBWayPaymentMethod>
