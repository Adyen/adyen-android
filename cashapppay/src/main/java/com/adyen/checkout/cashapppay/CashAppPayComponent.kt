/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 6/3/2023.
 */

package com.adyen.checkout.cashapppay

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.CashAppPayPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes

class CashAppPayComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: CashAppPayConfiguration
) : BasePaymentComponent<CashAppPayConfiguration, CashAppPayInputData, CashAppPayOutputData, GenericComponentState<CashAppPayPaymentMethod>>(
    savedStateHandle,
    paymentMethodDelegate,
    configuration
) {

    override fun createComponentState(): GenericComponentState<CashAppPayPaymentMethod> {
        TODO("Not yet implemented")
    }

    override fun onInputDataChanged(inputData: CashAppPayInputData): CashAppPayOutputData {
        TODO("Not yet implemented")
    }

    override fun getSupportedPaymentMethodTypes() = PAYMENT_METHOD_TYPES

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<CashAppPayComponent, CashAppPayConfiguration> =
            GenericPaymentComponentProvider(CashAppPayComponent::class.java)

        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.CASH_APP_PAY)
    }
}
