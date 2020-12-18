/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.mbway

import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

private val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.MB_WAY)

/**
 * Component should not be instantiated directly. Instead use the PROVIDER object.
 *
 * @param paymentMethodDelegate [GenericPaymentMethodDelegate]
 * @param configuration [MBWayConfiguration]
 */
class MBWayComponent(paymentMethodDelegate: GenericPaymentMethodDelegate, configuration: MBWayConfiguration) :
    BasePaymentComponent<MBWayConfiguration, MBWayInputData, MBWayOutputData,
        PaymentComponentState<MBWayPaymentMethod>>(paymentMethodDelegate, configuration) {

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<MBWayComponent, MBWayConfiguration> = GenericPaymentComponentProvider(MBWayComponent::class.java)
    }

    override fun onInputDataChanged(inputData: MBWayInputData): MBWayOutputData {
        Logger.v(TAG, "onInputDataChanged")
        return MBWayOutputData(inputData.mobilePhoneNumber)
    }

    override fun createComponentState(): PaymentComponentState<MBWayPaymentMethod> {
        val paymentComponentData = PaymentComponentData<MBWayPaymentMethod>()
        val paymentMethod = MBWayPaymentMethod().apply {
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE
        }

        val mbWayOutputData = outputData
        if (mbWayOutputData != null) {
            paymentMethod.telephoneNumber = mbWayOutputData.mobilePhoneNumberField.value
        }
        paymentComponentData.paymentMethod = paymentMethod
        return PaymentComponentState(paymentComponentData, mbWayOutputData?.isValid == true)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES
}
