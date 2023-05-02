/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.eps

import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.EPSPaymentMethod
import com.adyen.checkout.eps.internal.provider.EPSComponentProvider
import com.adyen.checkout.issuerlist.internal.IssuerListComponent
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.EPS] payment method.
 */
class EPSComponent internal constructor(
    delegate: IssuerListDelegate<EPSPaymentMethod, EPSComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<EPSComponentState>,
) : IssuerListComponent<EPSPaymentMethod, EPSComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler,
) {
    companion object {
        @JvmField
        val PROVIDER = EPSComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.EPS)
    }
}
