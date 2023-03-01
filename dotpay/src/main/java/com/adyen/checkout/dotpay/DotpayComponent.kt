/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.dotpay

import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.util.PaymentMethodTypes
import com.adyen.checkout.components.core.paymentmethod.DotpayPaymentMethod
import com.adyen.checkout.dotpay.DotpayComponent.Companion.PROVIDER
import com.adyen.checkout.dotpay.internal.provider.DotpayComponentProvider
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class DotpayComponent internal constructor(
    delegate: IssuerListDelegate<DotpayPaymentMethod, DotpayComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<DotpayComponentState>,
) : IssuerListComponent<DotpayPaymentMethod, DotpayComponentState>(
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
