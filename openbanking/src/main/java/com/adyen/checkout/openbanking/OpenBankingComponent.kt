/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.openbanking

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.OpenBankingPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate
import com.adyen.checkout.openbanking.OpenBankingComponent.Companion.PROVIDER

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class OpenBankingComponent internal constructor(
    savedStateHandle: SavedStateHandle,
    delegate: IssuerListDelegate<OpenBankingPaymentMethod>,
    configuration: OpenBankingConfiguration
) : IssuerListComponent<OpenBankingPaymentMethod>(
    savedStateHandle,
    delegate,
    configuration
) {
    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OpenBankingComponent, OpenBankingConfiguration> =
            OpenBankingComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.OPEN_BANKING)
    }
}
