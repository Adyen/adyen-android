/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/3/2022.
 */
package com.adyen.checkout.googlepay

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.GooglePayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.google.android.gms.wallet.PaymentData

class GooglePayComponentState(
    paymentComponentData: PaymentComponentData<GooglePayPaymentMethod>,
    isInputValid: Boolean,
    isReady: Boolean,
    val paymentData: PaymentData,
) : PaymentComponentState<GooglePayPaymentMethod>(paymentComponentData, isInputValid, isReady)
