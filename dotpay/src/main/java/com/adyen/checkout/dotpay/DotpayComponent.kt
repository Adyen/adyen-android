/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.dotpay

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.model.payments.request.DotpayPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.dotpay.DotpayComponent.Companion.PROVIDER
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class DotpayComponent internal constructor(
    delegate: IssuerListDelegate<DotpayPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<PaymentComponentState<DotpayPaymentMethod>>,
) : IssuerListComponent<DotpayPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler,
) {
    companion object {
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.DOTPAY)

        @JvmField
        val PROVIDER = DotpayComponentProvider()
    }
}
