/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.mbway

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultMBWayDelegate(val paymentMethod: PaymentMethod) : MBWayDelegate {

    private val _outputDataFlow = MutableStateFlow<MBWayOutputData?>(null)
    override val outputDataFlow: Flow<MBWayOutputData?> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<MBWayPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<MBWayPaymentMethod>?> = _componentStateFlow

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun onInputDataChanged(inputData: MBWayInputData) {
        Logger.v(TAG, "onInputDataChanged")
        val sanitizedNumber = inputData.localPhoneNumber.trimStart('0')
        val outputData = MBWayOutputData(inputData.countryCode + sanitizedNumber)
        outputDataChanged(outputData)
        createComponentState(outputData)
    }

    private fun outputDataChanged(outputData: MBWayOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    override fun createComponentState(outputData: MBWayOutputData) {
        val paymentMethod = MBWayPaymentMethod(
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE,
            telephoneNumber = outputData.mobilePhoneNumberFieldState.value
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

    private fun componentStateChanged(componentState: PaymentComponentState<MBWayPaymentMethod>) {
        _componentStateFlow.tryEmit(componentState)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
