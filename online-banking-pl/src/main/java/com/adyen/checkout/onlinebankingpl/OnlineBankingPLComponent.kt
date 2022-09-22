/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/9/2022.
 */

package com.adyen.checkout.onlinebankingpl

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.OnlineBankingPLPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent

class OnlineBankingPLComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: OnlineBankingPLConfiguration
) : IssuerListComponent<OnlineBankingPLPaymentMethod>(
    savedStateHandle,
    paymentMethodDelegate,
    configuration
) {
    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OnlineBankingPLComponent, OnlineBankingPLConfiguration> =
            GenericPaymentComponentProvider(OnlineBankingPLComponent::class.java)
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_PL)
    }

    override fun instantiateTypedPaymentMethod(): OnlineBankingPLPaymentMethod {
        return OnlineBankingPLPaymentMethod()
    }
}
