/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/1/2023.
 */

package com.adyen.checkout.onlinebankingjp

import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingJPPaymentMethod
import com.adyen.checkout.econtext.EContextComponent
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.onlinebankingjp.internal.provider.OnlineBankingJPComponentProvider

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.ECONTEXT_ONLINE] payment method.
 */
class OnlineBankingJPComponent internal constructor(
    delegate: EContextDelegate<OnlineBankingJPPaymentMethod, OnlineBankingJPComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<OnlineBankingJPComponentState>
) : EContextComponent<OnlineBankingJPPaymentMethod, OnlineBankingJPComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {
    companion object {
        @JvmField
        val PROVIDER = OnlineBankingJPComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ECONTEXT_ONLINE)
    }
}
