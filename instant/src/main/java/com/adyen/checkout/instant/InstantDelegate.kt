/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/11/2022.
 */

package com.adyen.checkout.instant

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import kotlinx.coroutines.flow.Flow

interface InstantDelegate : PaymentComponentDelegate<PaymentComponentState<PaymentMethodDetails>> {
    val componentStateFlow: Flow<PaymentComponentState<PaymentMethodDetails>>
}
