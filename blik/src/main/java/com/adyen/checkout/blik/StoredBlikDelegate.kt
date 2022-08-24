/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/7/2022.
 */

package com.adyen.checkout.blik

import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class StoredBlikDelegate(val storedPaymentMethod: StoredPaymentMethod) : BlikDelegate {

    private val _outputDataFlow = MutableStateFlow<BlikOutputData?>(null)
    override val outputDataFlow: Flow<BlikOutputData?> = _outputDataFlow

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<BlikPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>?> = _componentStateFlow

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    init {
        // this stored component does not require any input currently, we just generate a valid ComponentState right when it loads
        // BlikOutputData is not needed by createComponentState
        createComponentState(BlikOutputData(""))
    }

    override fun onInputDataChanged(inputData: BlikInputData) {
        Logger.e(TAG, "onInputDataChanged should not be called in StoredBlikDelegate")
    }

    private fun outputDataChanged(outputData: BlikOutputData) {
        _outputDataFlow.tryEmit(outputData)
    }

    override fun createComponentState(outputData: BlikOutputData) {
        val paymentMethod = BlikPaymentMethod(
            type = BlikPaymentMethod.PAYMENT_METHOD_TYPE,
            storedPaymentMethodId = storedPaymentMethod.id
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = paymentMethod
        )

        val paymentComponentState = PaymentComponentState(
            data = paymentComponentData,
            isInputValid = true,
            isReady = true
        )

        componentStateChanged(paymentComponentState)
    }

    override fun requiresInput(): Boolean = false

    private fun componentStateChanged(componentState: PaymentComponentState<BlikPaymentMethod>) {
        _componentStateFlow.tryEmit(componentState)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
