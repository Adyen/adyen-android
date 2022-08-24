/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.eps

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.model.payments.request.EPSPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class EPSComponent(
    savedStateHandle: SavedStateHandle,
    issuerListDelegate: IssuerListDelegate<EPSPaymentMethod>,
    configuration: EPSConfiguration
) : IssuerListComponent<EPSPaymentMethod>(savedStateHandle, issuerListDelegate, configuration) {

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<EPSComponent, EPSConfiguration> = EPSComponentProvider()
        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.EPS)
    }
}
