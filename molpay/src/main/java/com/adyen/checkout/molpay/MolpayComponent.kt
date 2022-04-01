/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */
package com.adyen.checkout.molpay

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.MolpayPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent

/**
 * PaymentComponent to handle iDeal payments.
 */
class MolpayComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: MolpayConfiguration
) : IssuerListComponent<MolpayPaymentMethod>(savedStateHandle, paymentMethodDelegate, configuration) {

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun instantiateTypedPaymentMethod(): MolpayPaymentMethod {
        return MolpayPaymentMethod()
    }

    companion object {
        val PROVIDER: PaymentComponentProvider<MolpayComponent, MolpayConfiguration> = GenericPaymentComponentProvider(
            MolpayComponent::class.java
        )
        val PAYMENT_METHOD_TYPES = arrayOf(
            PaymentMethodTypes.MOLPAY_THAILAND,
            PaymentMethodTypes.MOLPAY_MALAYSIA,
            PaymentMethodTypes.MOLPAY_VIETNAM
        )
    }
}
