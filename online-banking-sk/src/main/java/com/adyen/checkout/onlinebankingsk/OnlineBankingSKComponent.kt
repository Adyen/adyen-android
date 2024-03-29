/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.onlinebankingsk

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingSKPaymentMethod
import com.adyen.checkout.onlinebankingcore.internal.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.internal.ui.OnlineBankingDelegate
import com.adyen.checkout.onlinebankingsk.internal.provider.OnlineBankingSKComponentProvider

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.ONLINE_BANKING_SK] payment method.
 */
class OnlineBankingSKComponent internal constructor(
    delegate: OnlineBankingDelegate<OnlineBankingSKPaymentMethod, OnlineBankingSKComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<OnlineBankingSKComponentState>,
) : OnlineBankingComponent<OnlineBankingSKPaymentMethod, OnlineBankingSKComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {
    companion object {

        internal const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_sk.pdf"

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ONLINE_BANKING_SK)

        @JvmField
        val PROVIDER = OnlineBankingSKComponentProvider()
    }
}
