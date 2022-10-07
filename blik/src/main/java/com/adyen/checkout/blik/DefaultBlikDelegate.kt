/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik

import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultBlikDelegate(
    override val configuration: BlikConfiguration,
    val paymentMethod: PaymentMethod
) : BlikDelegate {

    private val inputData: BlikInputData = BlikInputData()

    private val _outputDataFlow = MutableStateFlow<BlikOutputData?>(null)
    override val outputDataFlow: Flow<BlikOutputData?> = _outputDataFlow

    override val outputData: BlikOutputData?
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<BlikPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>?> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(BlikComponentViewType)

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    override fun updateInputData(update: BlikInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        Logger.v(TAG, "onInputDataChanged")
        val outputData = createOutputData()
        outputDataChanged(outputData)
        createComponentState(outputData)
    }

    private fun createOutputData() = BlikOutputData(inputData.blikCode)

    private fun outputDataChanged(outputData: BlikOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    @VisibleForTesting
    internal fun createComponentState(outputData: BlikOutputData) {
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

    override fun getViewProvider(): ViewProvider = BlikViewProvider

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
