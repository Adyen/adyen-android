/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */
package com.adyen.checkout.sepa

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.model.payments.request.SepaPaymentMethod
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger

class SepaComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: SepaConfiguration
) : BasePaymentComponent<SepaConfiguration, SepaInputData, SepaOutputData, PaymentComponentState<SepaPaymentMethod>>(
    savedStateHandle,
    paymentMethodDelegate,
    configuration
) {

    override var inputData: SepaInputData = SepaInputData()

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: SepaInputData) {
        Logger.v(TAG, "onInputDataChanged")
        notifyOutputDataChanged(SepaOutputData(inputData.name, inputData.iban))
        createComponentState()
    }

    private fun createComponentState() {
        val sepaOutputData = outputData
        val paymentComponentData = PaymentComponentData<SepaPaymentMethod>()
        val paymentMethod = SepaPaymentMethod()
        paymentMethod.type = SepaPaymentMethod.PAYMENT_METHOD_TYPE
        if (sepaOutputData != null) {
            paymentMethod.ownerName = sepaOutputData.ownerNameField.value
            paymentMethod.iban = sepaOutputData.ibanNumberField.value
        }
        paymentComponentData.paymentMethod = paymentMethod
        notifyStateChanged(
            PaymentComponentState(
                paymentComponentData,
                sepaOutputData != null && sepaOutputData.isValid,
                true
            )
        )
    }

    companion object {
        private val TAG = getTag()
        val PROVIDER: PaymentComponentProvider<SepaComponent, SepaConfiguration> = SepaComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SEPA)
    }
}
