/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.PaymentComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.checkout.components.model.payments.request.IssuerListPaymentMethod
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListComponentParams
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListInputData
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListOutputData
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
