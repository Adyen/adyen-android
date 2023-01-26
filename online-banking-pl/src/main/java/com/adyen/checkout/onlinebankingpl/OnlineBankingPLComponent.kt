/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.onlinebankingpl

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentProviderOld
import com.adyen.checkout.components.model.payments.request.OnlineBankingPLPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent.Companion.PROVIDER

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class OnlineBankingPLComponent internal constructor(
    delegate: IssuerListDelegate<OnlineBankingPLPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
) : IssuerListComponent<OnlineBankingPLPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
) {
    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProviderOld<OnlineBankingPLComponent, OnlineBankingPLConfiguration> =
            OnlineBankingPLComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ONLINE_BANKING_PL)
    }
}
