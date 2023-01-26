/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.ideal

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentProviderOld
import com.adyen.checkout.components.model.payments.request.IdealPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.ideal.IdealComponent.Companion.PROVIDER
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class IdealComponent internal constructor(
    delegate: IssuerListDelegate<IdealPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
) : IssuerListComponent<IdealPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
) {

    companion object {
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.IDEAL)

        @JvmField
        val PROVIDER: PaymentComponentProviderOld<IdealComponent, IdealConfiguration> = IdealComponentProvider()
    }
}
