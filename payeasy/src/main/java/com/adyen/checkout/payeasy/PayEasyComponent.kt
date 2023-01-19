/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/1/2023.
 */

package com.adyen.checkout.payeasy

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.PayEasyPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.econtext.EContextComponent
import com.adyen.checkout.econtext.EContextDelegate

class PayEasyComponent internal constructor(
    delegate: EContextDelegate<PayEasyPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent
) : EContextComponent<PayEasyPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent
) {
    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<PayEasyComponent, PayEasyConfiguration> =
            PayEasyComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ECONTEXT_ATM)
    }
}
