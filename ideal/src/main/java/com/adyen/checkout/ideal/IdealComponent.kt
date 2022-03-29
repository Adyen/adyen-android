/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.ideal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.IdealPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent

/**
 * PaymentComponent to handle iDeal payments.
 */
class IdealComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: IdealConfiguration
) : IssuerListComponent<IdealPaymentMethod>(savedStateHandle, paymentMethodDelegate, configuration) {

    override val supportedPaymentMethodTypes: Array<String> = arrayOf(PaymentMethodTypes.IDEAL)

    override fun instantiateTypedPaymentMethod(): IdealPaymentMethod {
        return IdealPaymentMethod()
    }

    companion object {
        val PROVIDER: PaymentComponentProvider<IdealComponent, IdealConfiguration> = GenericPaymentComponentProvider(
            IdealComponent::class.java
        )
    }
}
