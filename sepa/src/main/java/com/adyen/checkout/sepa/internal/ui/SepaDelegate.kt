/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/7/2022.
 */

package com.adyen.checkout.sepa.internal.ui

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.sepa.internal.ui.model.SepaInputData
import com.adyen.checkout.sepa.internal.ui.model.SepaOutputData
import kotlinx.coroutines.flow.Flow

internal interface SepaDelegate :
    PaymentComponentDelegate<PaymentComponentState<SepaPaymentMethod>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: SepaOutputData

    val outputDataFlow: Flow<SepaOutputData>

    val componentStateFlow: Flow<PaymentComponentState<SepaPaymentMethod>>

    fun updateInputData(update: SepaInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
