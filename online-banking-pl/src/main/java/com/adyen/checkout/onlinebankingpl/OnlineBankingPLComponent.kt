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
import com.adyen.checkout.components.ui.ViewProvidingComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListComponent
import com.adyen.checkout.issuerlist.IssuerListDelegate
import com.adyen.checkout.onlinebankingpl.OnlineBankingPLComponent.Companion.PROVIDER
import kotlinx.coroutines.flow.Flow

/**
 * Component should not be instantiated directly. Instead use the [PROVIDER] object.
 */
class OnlineBankingPLComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: IssuerListDelegate<OnlineBankingPLPaymentMethod>,
    configuration: OnlineBankingPLConfiguration
) : IssuerListComponent<OnlineBankingPLPaymentMethod>(
    savedStateHandle,
    delegate,
    configuration
),
    ViewProvidingComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<OnlineBankingPLComponent, OnlineBankingPLConfiguration> =
            OnlineBankingPLComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.ONLINE_BANKING_PL)
    }
}
