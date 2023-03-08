/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.openbanking.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.paymentmethod.OpenBankingPaymentMethod
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.openbanking.OpenBankingComponent
import com.adyen.checkout.openbanking.OpenBankingComponentState
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.components.core.internal.ui.model.SessionParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class OpenBankingComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : IssuerListComponentProvider<
    OpenBankingComponent,
    OpenBankingConfiguration,
    OpenBankingPaymentMethod,
    OpenBankingComponentState
    >(
    componentClass = OpenBankingComponent::class.java,
    overrideComponentParams = overrideComponentParams,
    overrideSessionParams = overrideSessionParams,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<OpenBankingPaymentMethod, OpenBankingComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<OpenBankingComponentState>
    ) = OpenBankingComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<OpenBankingPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = OpenBankingComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = OpenBankingPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = OpenBankingComponent.PAYMENT_METHOD_TYPES
}
