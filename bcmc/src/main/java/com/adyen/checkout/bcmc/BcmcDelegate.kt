/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2022.
 */

package com.adyen.checkout.bcmc

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow

interface BcmcDelegate :
    PaymentComponentDelegate<PaymentComponentState<CardPaymentMethod>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    override val componentParams: BcmcComponentParams

    val outputData: BcmcOutputData

    val outputDataFlow: Flow<BcmcOutputData>

    val componentStateFlow: Flow<PaymentComponentState<CardPaymentMethod>>

    val exceptionFlow: Flow<CheckoutException>

    fun isCardNumberSupported(cardNumber: String?): Boolean

    fun updateInputData(update: BcmcInputData.() -> Unit)
}
