/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import kotlinx.coroutines.flow.Flow

interface BlikDelegate :
    PaymentMethodDelegate<
        BlikConfiguration,
        BlikInputData,
        BlikOutputData,
        PaymentComponentState<BlikPaymentMethod>
        > {

    val outputDataFlow: Flow<BlikOutputData?>

    val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>?>

    fun requiresInput(): Boolean
}
