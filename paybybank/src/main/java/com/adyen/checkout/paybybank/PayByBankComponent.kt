/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.issuerlist.IssuerListDelegate
import com.adyen.checkout.issuerlist.IssuerListOutputData
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

class PayByBankComponent(
    savedStateHandle: SavedStateHandle,
    override val delegate: PayByBankDelegate,
    private val issuerListDelegate: IssuerListDelegate<PayByBankPaymentMethod>,
    configuration: PayByBankConfiguration
) : BasePaymentComponent<PayByBankConfiguration, PaymentComponentState<PayByBankPaymentMethod>>(
    savedStateHandle,
    delegate,
    configuration
) {

    init {
        // FIXME temp
        delegate.outputDataFlow
            .filterNotNull()
            .combine(issuerListDelegate.outputDataFlow.filterNotNull()) { a: PayByBankOutputData, b: IssuerListOutputData ->
                a to b
            }
            .onEach {

            }
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<PayByBankComponent, PayByBankConfiguration> =
            PayByBankComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.PAY_BY_BANK)
    }
}
