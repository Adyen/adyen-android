/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/1/2023.
 */

package com.adyen.checkout.econtext

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.EContextPaymentMethod
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

interface EContextDelegate<EContextPaymentMethodT : EContextPaymentMethod> :
    PaymentComponentDelegate<PaymentComponentState<EContextPaymentMethodT>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: EContextOutputData

    val outputDataFlow: Flow<EContextOutputData>

    val componentStateFlow: Flow<PaymentComponentState<EContextPaymentMethodT>>

    fun updateInputData(update: EContextInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
