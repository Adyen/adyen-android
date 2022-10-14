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
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class StoredBlikDelegate(
    override val configuration: BlikConfiguration,
    val storedPaymentMethod: StoredPaymentMethod
) : BlikDelegate {

    private val _outputDataFlow = MutableStateFlow(createOutputData())
    override val outputDataFlow: Flow<BlikOutputData> = _outputDataFlow

    override val outputData: BlikOutputData
        get() = _outputDataFlow.value

    private val _componentStateFlow = MutableStateFlow<PaymentComponentState<BlikPaymentMethod>?>(null)
    override val componentStateFlow: Flow<PaymentComponentState<BlikPaymentMethod>?> = _componentStateFlow

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(BlikComponentViewType)

    override fun getPaymentMethodType(): String {
        return storedPaymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }

    init {
        // this stored component does not require any input currently, we just generate a valid ComponentState right when it loads
        // BlikOutputData is not needed by createComponentState
        createComponentState(outputData)
    }

    override fun updateInputData(update: BlikInputData.() -> Unit) {
        Logger.e(TAG, "updateInputData should not be called in StoredBlikDelegate")
    }

    private fun createOutputData() = BlikOutputData(blikCode = "")

    private fun createComponentState(outputData: BlikOutputData) {
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

    override fun getViewProvider(): ViewProvider = BlikViewProvider

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
