/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2022.
 */

package com.adyen.checkout.instant.internal.ui

import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import kotlinx.coroutines.flow.Flow

internal interface InstantPaymentDelegate :
    PaymentComponentDelegate<PaymentComponentState<PaymentMethodDetails>> {
    val componentStateFlow: Flow<PaymentComponentState<PaymentMethodDetails>>
}
