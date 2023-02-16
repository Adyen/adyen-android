/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/9/2022.
 */

package com.adyen.checkout.paybybank.internal.ui

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.paybybank.internal.ui.model.PayByBankInputData
import com.adyen.checkout.paybybank.internal.ui.model.PayByBankOutputData
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
