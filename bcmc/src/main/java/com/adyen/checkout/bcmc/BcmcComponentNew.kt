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
import com.adyen.checkout.bcmc.internal.provider.BcmcComponentProviderNew
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler

class BcmcComponentNew(
    private val cardDelegate: CardDelegate,
    private val genericActionDelegate: GenericActionDelegate,
    private val actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<CardComponentState>,
) : CardComponent(cardDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler) {
    companion object {
        @JvmField
        val PROVIDER = BcmcComponentProviderNew()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.BCMC)
    }
}
