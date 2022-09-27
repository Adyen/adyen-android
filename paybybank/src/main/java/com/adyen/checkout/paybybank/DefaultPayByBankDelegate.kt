/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PayByBankPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DefaultPayByBankDelegate(
    val paymentMethod: PaymentMethod
) : PayByBankDelegate {

    private val _outputDataFlow = MutableStateFlow<PayByBankOutputData?>(null)
    override val outputDataFlow: Flow<PayByBankOutputData?> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<PayByBankPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<PayByBankPaymentMethod>?> = _componentStateFlow

    override fun getPaymentMethodType(): String = paymentMethod.type ?: PaymentMethodTypes.UNKNOWN

    override fun onInputDataChanged(inputData: PayByBankInputData) {
        // TODO create output data
    }

    override fun createComponentState(outputData: PayByBankOutputData) {
        // TODO state
    }
}
