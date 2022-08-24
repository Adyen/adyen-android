/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultBlikDelegate(val paymentMethod: PaymentMethod) : BlikDelegate {

    private val _outputDataFlow = MutableStateFlow<BlikOutputData?>(null)
    override val outputDataFlow: Flow<BlikOutputData?> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<BlikPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>?> = _componentStateFlow

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onInputDataChanged(inputData: BlikInputData) {
        Logger.v(TAG, "onInputDataChanged")
        val outputData = BlikOutputData(inputData.blikCode)
        outputDataChanged(outputData)
        createComponentState(outputData)
    }

    private fun outputDataChanged(outputData: BlikOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    override fun createComponentState(outputData: BlikOutputData) {
        val paymentMethod = BlikPaymentMethod(
            type = BlikPaymentMethod.PAYMENT_METHOD_TYPE,
            blikCode = outputData.blikCodeField.value
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod
        )

        val paymentComponentState = PaymentComponentState(
            data = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true
        )

        componentStateChanged(paymentComponentState)
    }

    private fun componentStateChanged(componentState: PaymentComponentState<BlikPaymentMethod>) {
        _componentStateFlow.tryEmit(componentState)
    }

    override fun requiresInput(): Boolean = true

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
