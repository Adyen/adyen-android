/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2022.
 */

package com.adyen.checkout.issuerlist.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.components.core.paymentmethod.IssuerListPaymentMethod
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListComponentParams
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListInputData
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerListOutputData
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.ui.core.old.internal.ui.ButtonDelegate
import com.adyen.checkout.ui.core.old.internal.ui.UIStateDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface IssuerListDelegate<
    IssuerListPaymentMethodT : IssuerListPaymentMethod,
    ComponentStateT : PaymentComponentState<IssuerListPaymentMethodT>
    > :
    PaymentComponentDelegate<ComponentStateT>,
    ViewProvidingDelegate,
    ButtonDelegate,
    UIStateDelegate {

    override val componentParams: IssuerListComponentParams

    val outputData: IssuerListOutputData

    val outputDataFlow: Flow<IssuerListOutputData>

    val componentStateFlow: Flow<ComponentStateT>

    fun getIssuers(): List<IssuerModel>

    fun updateInputData(update: IssuerListInputData.() -> Unit)

    fun setInteractionBlocked(isInteractionBlocked: Boolean)
}
