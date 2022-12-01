/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.onlinebankingsk

import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.OnlineBankingSKPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.OnlineBankingDelegate

class OnlineBankingSKComponent internal constructor(
    delegate: OnlineBankingDelegate<OnlineBankingSKPaymentMethod>,
) : OnlineBankingComponent<OnlineBankingSKPaymentMethod>(delegate) {
    companion object {
        internal const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_sk.pdf"

        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_SK)

        @JvmField
        val PROVIDER: PaymentComponentProvider<
            OnlineBankingComponent<OnlineBankingSKPaymentMethod>, OnlineBankingSKConfiguration
            > = OnlineBankingSKComponentProvider()
    }
}
