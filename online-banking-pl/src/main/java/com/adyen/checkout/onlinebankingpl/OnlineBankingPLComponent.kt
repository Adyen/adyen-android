/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.onlinebankingpl

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.OnlineBankingPLPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class OnlineBankingPLComponent(
    savedStateHandle: SavedStateHandle,
    issuerListDelegate: IssuerListDelegate<OnlineBankingPLPaymentMethod>,
    configuration: OnlineBankingPLConfiguration
) : IssuerListComponent<OnlineBankingPLPaymentMethod>(
    savedStateHandle,
    issuerListDelegate,
    configuration
) {
    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OnlineBankingPLComponent, OnlineBankingPLConfiguration> =
            OnlineBankingPLComponentProvider()
        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_PL)
    }
}
