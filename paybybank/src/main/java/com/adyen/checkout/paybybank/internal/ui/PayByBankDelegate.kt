/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank.internal.ui

import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.components.core.paymentmethod.PayByBankPaymentMethod
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.paybybank.internal.ui.model.PayByBankInputData
import com.adyen.checkout.paybybank.internal.ui.model.PayByBankOutputData
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

internal interface PayByBankDelegate :
    PaymentComponentDelegate<PaymentComponentState<PayByBankPaymentMethod>>,
    ViewProvidingDelegate,
    UIStateDelegate {

    val outputData: PayByBankOutputData

    val outputDataFlow: Flow<PayByBankOutputData>

    val componentStateFlow: Flow<PaymentComponentState<PayByBankPaymentMethod>>

    fun getIssuers(): List<IssuerModel>

    fun updateInputData(update: PayByBankInputData.() -> Unit)

    fun onSubmit()

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
