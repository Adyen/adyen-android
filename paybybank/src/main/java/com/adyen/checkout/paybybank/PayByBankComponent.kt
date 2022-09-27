/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PayByBankComponent(
    savedStateHandle: SavedStateHandle,
    private val payByBankDelegate: PayByBankDelegate,
    configuration: PayByBankConfiguration
) : BasePaymentComponent<PayByBankConfiguration, PayByBankInputData, PayByBankOutputData,
    PaymentComponentState<PayByBankPaymentMethod>>(savedStateHandle, payByBankDelegate, configuration) {

    override val inputData: PayByBankInputData = PayByBankInputData()

    init {
        payByBankDelegate.outputDataFlow
            .filterNotNull()
            .onEach { notifyOutputDataChanged(it) }
            .launchIn(viewModelScope)

        payByBankDelegate.componentStateFlow
            .filterNotNull()
            .onEach { notifyStateChanged(it) }
            .launchIn(viewModelScope)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: PayByBankInputData) {
        payByBankDelegate.onInputDataChanged(inputData)
    }

    companion object {
        @JvmField
        val PROVIDER: PaymentComponentProvider<PayByBankComponent, PayByBankConfiguration> =
            PayByBankComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.PAY_BY_BANK)
    }
}
