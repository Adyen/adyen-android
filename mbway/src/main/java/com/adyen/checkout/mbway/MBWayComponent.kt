/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */
package com.adyen.checkout.mbway

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.PaymentComponentProvider
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
private const val ISO_CODE_PORTUGAL = "PT"
private const val ISO_CODE_SPAIN = "ES"
private val SUPPORTED_COUNTRIES = listOf(ISO_CODE_PORTUGAL, ISO_CODE_SPAIN)

/**
 * Component should not be instantiated directly. Instead use the PROVIDER object.
 *
 * @param paymentMethodDelegate [GenericPaymentMethodDelegate]
 * @param configuration [MBWayConfiguration]
 */
class MBWayComponent(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: GenericPaymentMethodDelegate,
    configuration: MBWayConfiguration
) :
    BasePaymentComponent<MBWayConfiguration, MBWayInputData, MBWayOutputData,
        GenericComponentState<MBWayPaymentMethod>>(savedStateHandle, paymentMethodDelegate, configuration) {

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<MBWayComponent, MBWayConfiguration> = GenericPaymentComponentProvider(MBWayComponent::class.java)
    }

    override fun onInputDataChanged(inputData: MBWayInputData): MBWayOutputData {
        Logger.v(TAG, "onInputDataChanged")
        return MBWayOutputData(getPhoneNumber(inputData))
    }

    private fun getPhoneNumber(inputData: MBWayInputData): String {
        val sanitizedNumber = inputData.localPhoneNumber.trimStart('0')
        return inputData.countryCode + sanitizedNumber
    }

    override fun createComponentState(): GenericComponentState<MBWayPaymentMethod> {
        val paymentComponentData = PaymentComponentData<MBWayPaymentMethod>()
        val paymentMethod = MBWayPaymentMethod().apply {
            type = MBWayPaymentMethod.PAYMENT_METHOD_TYPE
        }

        val mbWayOutputData = outputData
        if (mbWayOutputData != null) {
            paymentMethod.telephoneNumber = mbWayOutputData.mobilePhoneNumberFieldState.value
        }
        paymentComponentData.paymentMethod = paymentMethod
        return GenericComponentState(paymentComponentData, mbWayOutputData?.isValid == true, true)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES

    fun getSupportedCountries(): List<String> = SUPPORTED_COUNTRIES
}
