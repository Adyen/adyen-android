/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/8/2023.
 */

package com.adyen.checkout.bcmc

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.bcmc.internal.provider.BcmcComponentProvider
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.BCMC] payment method.
 */
class BcmcComponent internal constructor(
    cardDelegate: CardDelegate,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<BcmcComponentState>,
) : CardComponent(cardDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler) {
    companion object {
        @JvmField
        val PROVIDER = BcmcComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.BCMC)
    }
}
