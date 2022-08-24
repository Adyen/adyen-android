/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2022.
 */

package com.adyen.checkout.bcmc

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface BcmcDelegate :
    PaymentMethodDelegate<
        BcmcConfiguration,
        BcmcInputData,
        BcmcOutputData,
        PaymentComponentState<CardPaymentMethod>
        > {

    val outputDataFlow: Flow<BcmcOutputData?>

    val componentStateFlow: Flow<PaymentComponentState<CardPaymentMethod>?>

    val exceptionFlow: Flow<CheckoutException>

    fun initialize(coroutineScope: CoroutineScope)

    fun isCardNumberSupported(cardNumber: String?): Boolean
}
