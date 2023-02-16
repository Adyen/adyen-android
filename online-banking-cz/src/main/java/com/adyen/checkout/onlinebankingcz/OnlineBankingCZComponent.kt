/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import com.adyen.checkout.action.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.ComponentEventHandler
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.internal.ui.OnlineBankingDelegate
import com.adyen.checkout.onlinebankingcz.internal.provider.OnlineBankingCZComponentProvider

class OnlineBankingCZComponent internal constructor(
    delegate: OnlineBankingDelegate<OnlineBankingCZPaymentMethod>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<PaymentComponentState<OnlineBankingCZPaymentMethod>>
) : OnlineBankingComponent<OnlineBankingCZPaymentMethod>(
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

        internal const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_cs.pdf"

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(PaymentMethodTypes.ONLINE_BANKING_CZ)

        @JvmField
        val PROVIDER = OnlineBankingCZComponentProvider()
    }
}
