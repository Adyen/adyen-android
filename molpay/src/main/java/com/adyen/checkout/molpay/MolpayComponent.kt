/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */
package com.adyen.checkout.molpay

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.components.core.internal.PaymentComponent
import com.adyen.checkout.components.core.paymentmethod.MolpayPaymentMethod
import com.adyen.checkout.issuerlist.internal.IssuerListComponent
import com.adyen.checkout.issuerlist.internal.ui.IssuerListDelegate
import com.adyen.checkout.molpay.internal.provider.MolpayComponentProvider

/**
 * A [PaymentComponent] that supports the [PaymentMethodTypes.MOLPAY_MALAYSIA], [PaymentMethodTypes.MOLPAY_THAILAND]
 * and [PaymentMethodTypes.MOLPAY_VIETNAM] payment methods.
 */
class MolpayComponent internal constructor(
    delegate: IssuerListDelegate<MolpayPaymentMethod, MolpayComponentState>,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    componentEventHandler: ComponentEventHandler<MolpayComponentState>,
) : IssuerListComponent<MolpayPaymentMethod, MolpayComponentState>(
    delegate,
    genericActionDelegate,
    actionHandlingComponent,
    componentEventHandler,
) {

    companion object {
        @JvmField
        val PROVIDER = MolpayComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(
            PaymentMethodTypes.MOLPAY_MALAYSIA,
            PaymentMethodTypes.MOLPAY_THAILAND,
            PaymentMethodTypes.MOLPAY_VIETNAM,
        )
    }
}
