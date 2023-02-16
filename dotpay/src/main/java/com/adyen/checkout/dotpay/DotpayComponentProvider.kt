/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 12/4/2022.
 */

package com.adyen.checkout.dotpay

import androidx.annotation.RestrictTo
import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.base.ComponentParams
import com.adyen.checkout.components.model.payments.request.DotpayPaymentMethod
import com.adyen.checkout.issuerlist.internal.provider.IssuerListComponentProvider
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.sessions.model.setup.SessionSetupConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DotpayComponentProvider(
    overrideComponentParams: ComponentParams? = null,
    private val sessionSetupConfiguration: SessionSetupConfiguration? = null
) : IssuerListComponentProvider<DotpayComponent, DotpayConfiguration, DotpayPaymentMethod>(
    componentClass = DotpayComponent::class.java,
    overrideComponentParams = overrideComponentParams,
) {

    override fun createComponent(
        delegate: IssuerListDelegate<DotpayPaymentMethod>,
        genericActionDelegate: GenericActionDelegate,
        actionHandlingComponent: DefaultActionHandlingComponent,
        componentEventHandler: ComponentEventHandler<PaymentComponentState<DotpayPaymentMethod>>
    ) = DotpayComponent(
        delegate = delegate,
        genericActionDelegate = genericActionDelegate,
        actionHandlingComponent = actionHandlingComponent,
        componentEventHandler = componentEventHandler,
    )

    override fun createPaymentMethod() = DotpayPaymentMethod()

    override fun getSupportedPaymentMethods(): List<String> = DotpayComponent.PAYMENT_METHOD_TYPES
}
