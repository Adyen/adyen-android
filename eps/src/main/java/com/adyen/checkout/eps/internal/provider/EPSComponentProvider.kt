/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.eps.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.paymentmethod.EPSPaymentMethod
import com.adyen.checkout.eps.EPSComponent
import com.adyen.checkout.eps.EPSComponentState
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class EPSComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : IssuerListComponentProvider<EPSComponent, EPSConfiguration, EPSPaymentMethod, EPSComponentState>(
    componentClass = EPSComponent::class.java,
    overrideComponentParams = overrideComponentParams,
    overrideSessionParams = overrideSessionParams,
    hideIssuerLogosDefaultValue = true
) {

    override fun createComponent(
        delegate: IssuerListDelegate<EPSPaymentMethod, EPSComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<EPSComponentState>
    ) = EPSComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<EPSPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = EPSComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = EPSPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = EPSComponent.PAYMENT_METHOD_TYPES
}
