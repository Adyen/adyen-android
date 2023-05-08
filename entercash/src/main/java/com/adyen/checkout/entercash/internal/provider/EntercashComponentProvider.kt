/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.entercash.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.ui.model.ComponentParams
import com.adyen.checkout.components.core.internal.ui.model.SessionParams
import com.adyen.checkout.components.core.paymentmethod.EntercashPaymentMethod
import com.adyen.checkout.entercash.EntercashComponent
import com.adyen.checkout.entercash.EntercashComponentState
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

class EntercashComponentProvider
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor(
    overrideComponentParams: ComponentParams? = null,
    overrideSessionParams: SessionParams? = null,
) : IssuerListComponentProvider<
    EntercashComponent,
    EntercashConfiguration,
    EntercashPaymentMethod,
    EntercashComponentState
    >(
    componentClass = EntercashComponent::class.java,
    overrideComponentParams = overrideComponentParams,
    overrideSessionParams = overrideSessionParams,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<EntercashPaymentMethod, EntercashComponentState>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<EntercashComponentState>
    ) = EntercashComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createComponentState(
        data: PaymentComponentData<EntercashPaymentMethod>,
        isInputValid: Boolean,
        isReady: Boolean
    ) = EntercashComponentState(data, isInputValid, isReady)

    override fun createPaymentMethod() = EntercashPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = EntercashComponent.PAYMENT_METHOD_TYPES
}
