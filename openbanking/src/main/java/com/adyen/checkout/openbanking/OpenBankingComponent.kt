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
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.OpenBankingPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent

/**
 * PaymentComponent to handle iDeal payments.
 */
class OpenBankingComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: OpenBankingConfiguration
) : IssuerListComponent<OpenBankingPaymentMethod>(savedStateHandle, paymentMethodDelegate, configuration) {

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun instantiateTypedPaymentMethod(): OpenBankingPaymentMethod {
        return OpenBankingPaymentMethod()
    }

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OpenBankingComponent, OpenBankingConfiguration> = OpenBankingProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.OPEN_BANKING)
    }
}
