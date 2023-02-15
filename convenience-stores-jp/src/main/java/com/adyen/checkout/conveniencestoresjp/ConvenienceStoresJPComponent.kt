/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/1/2023.
 */

package com.adyen.checkout.conveniencestoresjp

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.model.payments.request.ConvenienceStoresJPPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.conveniencestoresjp.internal.provider.ConvenienceStoresJPComponentProvider
import com.adyen.checkout.econtext.EContextComponent
import com.adyen.checkout.econtext.internal.ui.EContextDelegate

class ConvenienceStoresJPComponent internal constructor(
    delegate: EContextDelegate<ConvenienceStoresJPPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<PaymentComponentState<ConvenienceStoresJPPaymentMethod>>
) : EContextComponent<ConvenienceStoresJPPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {
    companion object {
        @JvmField
        val PROVIDER = ConvenienceStoresJPComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ECONTEXT_STORES)
    }
}
