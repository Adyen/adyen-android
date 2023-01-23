/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2022.
 */

package com.adyen.checkout.issuerlist

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.components.ui.ButtonDelegate
import com.adyen.checkout.components.ui.UIStateDelegate
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

interface IssuerListDelegate<IssuerListPaymentMethodT : IssuerListPaymentMethod> :
    PaymentComponentDelegate<PaymentComponentState<IssuerListPaymentMethodT>>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    override val componentParams: IssuerListComponentParams

    val outputData: IssuerListOutputData

    val outputDataFlow: Flow<IssuerListOutputData>

    val componentStateFlow: Flow<PaymentComponentState<IssuerListPaymentMethodT>>

    fun getIssuers(): List<IssuerModel>

    fun updateInputData(update: IssuerListInputData.() -> Unit)
}
