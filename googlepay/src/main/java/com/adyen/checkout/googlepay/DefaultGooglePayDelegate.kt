/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/7/2022.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.googlepay.model.GooglePayParams
import com.adyen.checkout.googlepay.util.GooglePayUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class DefaultGooglePayDelegate(
    private val paymentMethod: PaymentMethod,
    override val configuration: GooglePayConfiguration,
) : GooglePayDelegate {

    private val inputData: GooglePayInputData = GooglePayInputData()

    private val _componentStateFlow = MutableStateFlow<GooglePayComponentState?>(null)
    override val componentStateFlow: Flow<GooglePayComponentState?> = _componentStateFlow

    override fun updateInputData(update: GooglePayInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
    }

    private fun onInputDataChanged() {
        val paymentData = inputData.paymentData ?: throw CheckoutException("paymentData is null")

        createComponentState(GooglePayOutputData(paymentData))
    }

    internal fun createComponentState(outputData: GooglePayOutputData) {
        val paymentMethod = GooglePayUtils.createGooglePayPaymentMethod(outputData.paymentData, paymentMethod.type)
        val paymentComponentData = PaymentComponentData(paymentMethod = paymentMethod)

        val componentState = GooglePayComponentState(
            paymentComponentData = paymentComponentData,
            isInputValid = outputData.isValid,
            isReady = true,
            paymentData = outputData.paymentData
        )

        _componentStateFlow.tryEmit(componentState)
    }

    override fun getGooglePayParams(): GooglePayParams {
        val config = paymentMethod.configuration
        val serverGatewayMerchantId = config?.gatewayMerchantId
        return GooglePayParams(configuration, serverGatewayMerchantId, paymentMethod.brands)
    }

    override fun getPaymentMethodType(): String {
        return paymentMethod.type ?: PaymentMethodTypes.UNKNOWN
    }
}
