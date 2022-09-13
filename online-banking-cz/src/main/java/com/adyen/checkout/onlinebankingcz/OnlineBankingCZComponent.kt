/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 8/9/2022.
 */

package com.adyen.checkout.onlinebankingcz

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.OnlineBankingCZPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.onlinebankingcore.OnlineBankingComponent
import com.adyen.checkout.onlinebankingcore.OnlineBankingConfiguration
import com.adyen.checkout.onlinebankingcore.OnlineBankingDelegate

class OnlineBankingCZComponent(
    savedStateHandle: SavedStateHandle,
    delegate: OnlineBankingDelegate<OnlineBankingCZPaymentMethod>,
    configuration: OnlineBankingConfiguration
) : OnlineBankingComponent<OnlineBankingCZPaymentMethod>(savedStateHandle, delegate, configuration) {

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OnlineBankingCZComponent, OnlineBankingConfiguration> =
            OnlineBankingCZComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_CZ)
    }
}
