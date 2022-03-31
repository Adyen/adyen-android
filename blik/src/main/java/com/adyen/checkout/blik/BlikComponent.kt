/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.blik

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.StoredPaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.base.GenericStoredPaymentComponentProvider
import com.adyen.checkout.components.base.GenericStoredPaymentDelegate
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger

class BlikComponent : BasePaymentComponent<BlikConfiguration, BlikInputData, BlikOutputData, PaymentComponentState<BlikPaymentMethod>> {
    constructor(
        savedStateHandle: SavedStateHandle,
        paymentMethodDelegate: GenericPaymentMethodDelegate,
        configuration: BlikConfiguration
    ) : super(savedStateHandle, paymentMethodDelegate, configuration)

    constructor(
        savedStateHandle: SavedStateHandle,
        paymentDelegate: GenericStoredPaymentDelegate,
        configuration: BlikConfiguration
    ) : super(savedStateHandle, paymentDelegate, configuration) {
        // TODO: 09/12/2020 move this logic to base component, maybe create the InputData from the delegate?
        inputDataChanged(BlikInputData())
    }

    override fun requiresInput(): Boolean {
        return paymentMethodDelegate is GenericPaymentMethodDelegate
    }

    override fun onInputDataChanged(inputData: BlikInputData): BlikOutputData {
        Logger.v(TAG, "onInputDataChanged")
        return BlikOutputData(inputData.blikCode)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    override fun createComponentState(): PaymentComponentState<BlikPaymentMethod> {
        val blikOutputData = outputData
        val paymentComponentData = PaymentComponentData<BlikPaymentMethod>()
        val paymentMethod = BlikPaymentMethod()
        paymentMethod.type = BlikPaymentMethod.PAYMENT_METHOD_TYPE
        if (blikOutputData != null) {
            paymentMethod.blikCode = blikOutputData.blikCodeField.value
        }
        if (paymentMethodDelegate is GenericStoredPaymentDelegate) {
            paymentMethod.storedPaymentMethodId = paymentMethodDelegate.storedPaymentMethod.id
        }
        paymentComponentData.paymentMethod = paymentMethod
        val isInputValid = (
            paymentMethodDelegate is GenericStoredPaymentDelegate ||
                blikOutputData != null &&
                blikOutputData.isValid
            )
        return PaymentComponentState(paymentComponentData, isInputValid, true)
    }

    companion object {
        private val TAG = getTag()

        @JvmField
        val PROVIDER: StoredPaymentComponentProvider<BlikComponent, BlikConfiguration> =
            GenericStoredPaymentComponentProvider(BlikComponent::class.java)
        val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.BLIK)
    }
}
