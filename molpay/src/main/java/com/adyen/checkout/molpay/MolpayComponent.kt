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
import com.adyen.checkout.components.model.payments.request.MolpayPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate
import com.adyen.checkout.molpay.MolpayComponent.Companion.PROVIDER

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class MolpayComponent internal constructor(
    savedStateHandle: SavedStateHandle,
    delegate: IssuerListDelegate<MolpayPaymentMethod>,
    configuration: MolpayConfiguration
) : IssuerListComponent<MolpayPaymentMethod>(
    savedStateHandle,
    delegate,
    configuration
) {

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<MolpayComponent, MolpayConfiguration> = MolpayComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(
            PaymentMethodTypes.MOLPAY_THAILAND,
            PaymentMethodTypes.MOLPAY_MALAYSIA,
            PaymentMethodTypes.MOLPAY_VIETNAM
        )
    }
}
