/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/1/2023.
 */

package com.adyen.checkout.seveneleven

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.SevenElevenPaymentMethod
import com.adyen.checkout.econtext.internal.EContextComponent
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.seveneleven.internal.provider.SevenElevenComponentProvider

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN] payment method.
 */
class SevenElevenComponent internal constructor(
    delegate: EContextDelegate<SevenElevenPaymentMethod, SevenElevenComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<SevenElevenComponentState>
) : EContextComponent<SevenElevenPaymentMethod, SevenElevenComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {
    companion object {
        @JvmField
        val PROVIDER = SevenElevenComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ECONTEXT_SEVEN_ELEVEN)
    }
}
