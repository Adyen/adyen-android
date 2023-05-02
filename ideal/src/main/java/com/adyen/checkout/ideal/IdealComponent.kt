/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.ideal

import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.IdealPaymentMethod
import com.adyen.checkout.ideal.internal.provider.IdealComponentProvider
import com.adyen.checkout.issuerlist.internal.IssuerListComponent
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.IDEAL] payment method.
 */
class IdealComponent internal constructor(
    delegate: IssuerListDelegate<IdealPaymentMethod, IdealComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<IdealComponentState>,
) : IssuerListComponent<IdealPaymentMethod, IdealComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler,
) {

    companion object {
        @JvmField
        val PROVIDER = IdealComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.IDEAL)
    }
}
