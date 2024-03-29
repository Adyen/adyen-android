/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/2/2023.
 */

package com.adyen.checkout.seveneleven

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.SevenElevenPaymentMethod

/**
 * Represents the state of [SevenElevenComponent].
 */
data class SevenElevenComponentState(
    override val data: PaymentComponentData<SevenElevenPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<SevenElevenPaymentMethod>
