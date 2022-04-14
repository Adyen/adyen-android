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
import com.adyen.checkout.core.log.AdyenLogger
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.sepa.di.SepaContainer

class SepaComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: SepaConfiguration,
    val logger: AdyenLogger,
    val sepaProcessor: SepaProcessor,
) : BasePaymentComponent<SepaConfiguration, SepaInputData, SepaOutputData, PaymentComponentState<SepaPaymentMethod>>(
    savedStateHandle,
    paymentMethodDelegate,
    configuration
) {

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun onInputDataChanged(inputData: SepaInputData): SepaOutputData {
        logger.v(TAG, "onInputDataChanged")
        return sepaProcessor(inputData)
    }

    override fun createComponentState(): PaymentComponentState<SepaPaymentMethod> {
        val paymentMethod = SepaPaymentMethod(
            type = SepaPaymentMethod.PAYMENT_METHOD_TYPE,
            ownerName = outputData?.ownerNameField?.value,
            iban = outputData?.ibanNumberField?.value
        )
        val paymentComponentData = PaymentComponentData<SepaPaymentMethod>().apply {
            this.paymentMethod = paymentMethod
        }
        return PaymentComponentState(
            data = paymentComponentData,
            isInputValid = outputData?.isValid == true,
            isReady = true
        )
    }

    companion object {
        init {
            SepaContainer.addToServiceLocator()
        }
        private val TAG = getTag()
        val PROVIDER: PaymentComponentProvider<SepaComponent, SepaConfiguration> = SepaComponentProvider()
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.SEPA)
    }
}
