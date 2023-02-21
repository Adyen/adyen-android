/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/2/2023.
 */

package com.adyen.checkout.onlinebankingjp

import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingJPPaymentMethod

data class OnlineBankingJPComponentState(
    override val data: PaymentComponentData<OnlineBankingJPPaymentMethod>,
    override val isInputValid: Boolean,
    override val isReady: Boolean
) : PaymentComponentState<OnlineBankingJPPaymentMethod>
