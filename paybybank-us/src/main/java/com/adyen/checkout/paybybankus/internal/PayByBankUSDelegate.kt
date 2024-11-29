/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2024.
 */

package com.adyen.checkout.paybybankus.internal

import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.paybybankus.PayByBankUSComponentState
import com.adyen.checkout.paybybankus.internal.ui.model.PayByBankUSOutputData
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface PayByBankUSDelegate :
    PaymentComponentDelegate<PayByBankUSComponentState>,
    ViewProvidingDelegate {

    val outputData: PayByBankUSOutputData

    val outputDataFlow: Flow<PayByBankUSOutputData>

    val componentStateFlow: Flow<PayByBankUSComponentState>

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
