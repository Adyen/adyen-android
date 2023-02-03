/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/1/2023.
 */

package com.adyen.checkout.ach

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.AchPaymentMethod
import com.adyen.checkout.components.ui.AddressDelegate
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow

interface AchDelegate :
    PaymentComponentDelegate<PaymentComponentState<AchPaymentMethod>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate,
    AddressDelegate {
    val outputData: AchOutputData

    val outputDataFlow: Flow<AchOutputData>

    val componentStateFlow: Flow<PaymentComponentState<AchPaymentMethod>>

    val exceptionFlow: Flow<CheckoutException>
    fun updateInputData(update: AchInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
