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
import com.adyen.checkout.components.model.payments.request.IdealPaymentMethod
import com.adyen.checkout.components.ui.ViewProvidingComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.ideal.IdealComponent.Companion.PROVIDER
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class IdealComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: IssuerListDelegate<IdealPaymentMethod>,
    configuration: IdealConfiguration
) : IssuerListComponent<IdealPaymentMethod>(
    savedStateHandle,
    delegate,
    configuration
),
    ViewProvidingComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.IDEAL)

        @JvmField
        val PROVIDER: PaymentComponentProvider<IdealComponent, IdealConfiguration> = IdealComponentProvider()
    }
}
