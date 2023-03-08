/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.onlinebankingpl.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingPLPaymentMethod
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponentState
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OnlineBankingPLComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : IssuerListComponentProvider<
    OnlineBankingPLComponent,
    OnlineBankingPLConfiguration,
    OnlineBankingPLPaymentMethod,
    OnlineBankingPLComponentState
    >(
    componentClass = OnlineBankingPLComponent::class.java,
    overrideComponentParams = overrideComponentParams,
    overrideSessionParams = overrideSessionParams,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<OnlineBankingPLPaymentMethod, OnlineBankingPLComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<OnlineBankingPLComponentState>
    ) = OnlineBankingPLComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<OnlineBankingPLPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = OnlineBankingPLComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = OnlineBankingPLPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = OnlineBankingPLComponent.PAYMENT_METHOD_TYPES
}
