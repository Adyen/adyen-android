/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.entercash

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.model.payments.request.EntercashPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.entercash.EntercashComponent.Companion.PROVIDER
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate
import com.adyen.checkout.sessions.provider.SessionPaymentComponentProvider

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class EntercashComponent internal constructor(
    delegate: IssuerListDelegate<EntercashPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<PaymentComponentState<EntercashPaymentMethod>>,
) : IssuerListComponent<EntercashPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler,
) {
    companion object {
        @JvmField
        val PROVIDER: SessionPaymentComponentProvider<
            EntercashComponent,
            EntercashConfiguration,
            PaymentComponentState<EntercashPaymentMethod>
            > = EntercashComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ENTERCASH)
    }
}
