/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.entercash

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.request.EntercashPaymentMethod
import com.adyen.checkout.issuerlist.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.IssuerListDelegate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class EntercashComponentProvider(
    overrideComponentParams: ComponentParams? = null,
) : IssuerListComponentProvider<EntercashComponent, EntercashConfiguration, EntercashPaymentMethod>(
    componentClass = EntercashComponent::class.java,
    overrideComponentParams = overrideComponentParams,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<EntercashPaymentMethod>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<EntercashPaymentMethod>>
    ) = EntercashComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createPaymentMethod() = EntercashPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = EntercashComponent.PAYMENT_METHOD_TYPES
}
