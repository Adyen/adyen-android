/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.OnlineBankingCZPaymentMethod
import com.adyen.checkout.onlinebankingcore.internal.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.internal.ui.OnlineBankingDelegate
import com.adyen.checkout.onlinebankingcz.internal.provider.OnlineBankingCZComponentProvider

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.ONLINE_BANKING_CZ] payment method.
 */
class OnlineBankingCZComponent internal constructor(
    delegate: OnlineBankingDelegate<OnlineBankingCZPaymentMethod, OnlineBankingCZComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<OnlineBankingCZComponentState>
) : OnlineBankingComponent<OnlineBankingCZPaymentMethod, OnlineBankingCZComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {
    companion object {

        internal const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_cs.pdf"

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ONLINE_BANKING_CZ)

        @JvmField
        val PROVIDER = OnlineBankingCZComponentProvider()
    }
}
