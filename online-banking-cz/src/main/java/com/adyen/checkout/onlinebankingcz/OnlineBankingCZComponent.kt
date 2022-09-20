/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.DefaultOnlineBankingDelegate
import com.adyen.checkout.onlinebankingcore.OnlineBankingConfiguration

class OnlineBankingCZComponent(
    savedStateHandle: SavedStateHandle,
    delegate: DefaultOnlineBankingDelegate<OnlineBankingCZPaymentMethod>,
    configuration: OnlineBankingConfiguration
) : OnlineBankingComponent<OnlineBankingCZPaymentMethod>(
    savedStateHandle,
    delegate,
    configuration
) {

    override val termsAndConditionsUrl: String
        get() = TERMS_CONDITIONS_URL

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        private const val TERMS_CONDITIONS_URL = "https://static.payu.com/sites/terms/files/payu_privacy_policy_cs.pdf"

        @JvmField
        val PROVIDER: PaymentComponentProvider<
            OnlineBankingComponent<OnlineBankingCZPaymentMethod>, OnlineBankingCZConfiguration
            > = OnlineBankingCZComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_CZ)
    }
}
