/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/9/2021.
 */
package com.adyen.checkout.giftcard

import com.adyen.checkout.components.GenericComponentState
import com.adyen.checkout.components.PaymentComponentProvider
import com.adyen.checkout.components.base.BasePaymentComponent
import com.adyen.checkout.components.base.GenericPaymentComponentProvider
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate
import com.adyen.checkout.components.model.payments.request.GiftCardPaymentMethod
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()

private val PAYMENT_METHOD_TYPES = arrayOf(PaymentMethodTypes.GIFTCARD)

/**
 * Component should not be instantiated directly. Instead use the PROVIDER object.
 *
 * @param paymentMethodDelegate [GenericPaymentMethodDelegate]
 * @param configuration [GiftCardConfiguration]
 */
class GiftCardComponent(paymentMethodDelegate: GenericPaymentMethodDelegate, configuration: GiftCardConfiguration) :
    BasePaymentComponent<GiftCardConfiguration, GiftCardInputData, GiftCardOutputData,
        GenericComponentState<GiftCardPaymentMethod>>(paymentMethodDelegate, configuration) {

    companion object {
        @JvmStatic
        val PROVIDER: PaymentComponentProvider<GiftCardComponent, GiftCardConfiguration> =
            GenericPaymentComponentProvider(GiftCardComponent::class.java)
    }

    override fun onInputDataChanged(inputData: GiftCardInputData): GiftCardOutputData {
        Logger.v(TAG, "onInputDataChanged")
        return GiftCardOutputData(cardNumber = inputData.cardNumber, pin = inputData.pin)
    }

    override fun createComponentState(): GenericComponentState<GiftCardPaymentMethod> {
        val paymentComponentData = PaymentComponentData<GiftCardPaymentMethod>()
        val paymentMethod = GiftCardPaymentMethod().apply {
            type = GiftCardPaymentMethod.PAYMENT_METHOD_TYPE
        }

        val giftcardOutputData = outputData
        if (giftcardOutputData != null) {
            // TODO encrypt and set up paymentMethod
        }
        paymentComponentData.paymentMethod = paymentMethod
        return GenericComponentState(paymentComponentData, giftcardOutputData?.isValid == true, true)
    }

    override fun getSupportedPaymentMethodTypes(): Array<String> = PAYMENT_METHOD_TYPES
}
