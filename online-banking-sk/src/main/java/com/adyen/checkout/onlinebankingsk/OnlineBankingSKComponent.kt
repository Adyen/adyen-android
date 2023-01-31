/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.onlinebankingsk

import com.adyen.checkout.action.DefaultActionHandlingComponent
import com.adyen.checkout.action.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.model.payments.request.OnlineBankingSKPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.OnlineBankingDelegate
import com.adyen.checkout.sessions.provider.SessionPaymentComponentProvider

class OnlineBankingSKComponent internal constructor(
    delegate: OnlineBankingDelegate<OnlineBankingSKPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<PaymentComponentState<OnlineBankingSKPaymentMethod>>,
) : OnlineBankingComponent<OnlineBankingSKPaymentMethod>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler
) {

    override fun setInteractionBlocked(isInteractionBlocked: Boolean) {
        (delegate as? OnlineBankingDelegate<*>)?.setInteractionBlocked(isInteractionBlocked)
            ?: Logger.e(TAG, "Payment component is not interactable, ignoring.")
    }

    companion object {
        private val TAG = LogUtil.getTag()

        internal const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_sk.pdf"

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ONLINE_BANKING_SK)

        @JvmField
        val PROVIDER: SessionPaymentComponentProvider<
            OnlineBankingComponent<OnlineBankingSKPaymentMethod>,
            OnlineBankingSKConfiguration,
            PaymentComponentState<OnlineBankingSKPaymentMethod>
            > = OnlineBankingSKComponentProvider()
    }
}
