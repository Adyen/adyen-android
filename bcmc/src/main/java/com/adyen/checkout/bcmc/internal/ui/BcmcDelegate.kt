/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/7/2022.
 */

package com.adyen.checkout.bcmc.internal.ui

import com.adyen.checkout.bcmc.internal.ui.model.BcmcComponentParams
import com.adyen.checkout.bcmc.internal.ui.model.BcmcInputData
import com.adyen.checkout.bcmc.internal.ui.model.BcmcOutputData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface BcmcDelegate :
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

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
