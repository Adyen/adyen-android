/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/1/2023.
 */

package com.adyen.checkout.payeasy

import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.model.payments.request.PayEasyPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.econtext.EContextComponent
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.payeasy.internal.provider.PayEasyComponentProvider

class PayEasyComponent internal constructor(
    delegate: EContextDelegate<PayEasyPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<PaymentComponentState<PayEasyPaymentMethod>>
) : EContextComponent<PayEasyPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {
    companion object {
        @JvmField
        val PROVIDER = PayEasyComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ECONTEXT_ATM)
    }
}
