/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik.internal.ui

import com.adyen.checkout.blik.internal.ui.model.BlikInputData
import com.adyen.checkout.blik.internal.ui.model.BlikOutputData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.components.core.paymentmethod.BlikPaymentMethod
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface BlikDelegate :
    PaymentComponentDelegate<PaymentComponentState<BlikPaymentMethod>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    val outputData: BlikOutputData

    val outputDataFlow: Flow<BlikOutputData>

    val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>>

    fun updateInputData(update: BlikInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
