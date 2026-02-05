/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/3/2022.
 */
package com.adyen.checkout.googlepay.old

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.GooglePayPaymentMethod
import com.google.android.gms.wallet.PaymentData

/**
 * Represents the state of [GooglePayComponent].
 */
data class GooglePayComponentState(
    override val data: PaymentComponentData<GooglePayPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean,
    val paymentData: PaymentData?,
) : PaymentComponentState<GooglePayPaymentMethod>
