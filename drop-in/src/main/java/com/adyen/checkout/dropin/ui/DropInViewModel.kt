/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.adyen.checkout.base.ComponentAvailableCallback
import com.adyen.checkout.base.Configuration
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.model.paymentmethods.RecurringDetail
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.checkComponentAvailability
import com.adyen.checkout.dropin.ui.paymentmethods.PaymentMethodsModel

class DropInViewModel(application: Application) : AndroidViewModel(application), ComponentAvailableCallback<Configuration> {

    companion object {
        val TAG = LogUtil.getTag()
    }

    val paymentMethodsModelLiveData: MutableLiveData<PaymentMethodsModel> = MutableLiveData()

    var paymentMethodsApiResponse: PaymentMethodsApiResponse = PaymentMethodsApiResponse()
        set(value) {
            if (value != paymentMethodsApiResponse) {
                field = value
                if (value.paymentMethods != null) {
                    onPaymentMethodsResponseChanged(value.paymentMethods.orEmpty() + value.storedPaymentMethods.orEmpty())
                }
            }
        }

    private val paymentMethodsModel = PaymentMethodsModel()

    override fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod, config: Configuration?) {
        Logger.d(TAG, "onAvailabilityResult - ${paymentMethod.type} $isAvailable")

        if (isAvailable) {
            addPaymentMethod(paymentMethod)
        }

        // TODO handle unavailable and only notify when all list is checked
    }

    private fun onPaymentMethodsResponseChanged(paymentMethods: List<PaymentMethod>) {
        Logger.d(TAG, "onPaymentMethodsResponseChanged")

        for (paymentMethod in paymentMethods) {
            val type = paymentMethod.type

            if (type == null) {
                Logger.e(TAG, "Unexpected null type on PaymentMethod")
                continue
            }

            // If details is empty we default back to redirect, otherwise we don't support it.
            if (!PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(type) && paymentMethod.details != null) {
                Logger.e(TAG, "PaymentMethod not yet supported - $type")
                continue
            }

            if (paymentMethod.details == null) {
                Logger.d(TAG, "Empty payment method type - $type")
                addPaymentMethod(paymentMethod)
                continue
            }

            Logger.d(TAG, "Checking availability for type - $type")

            checkComponentAvailability(getApplication(), paymentMethod, this)
        }
    }

    private fun addPaymentMethod(paymentMethod: PaymentMethod) {
        if (paymentMethod is RecurringDetail) {
            paymentMethodsModel.storedPaymentMethods.add(paymentMethod)
        } else {
            paymentMethodsModel.paymentMethods.add(paymentMethod)
        }
        paymentMethodsModelLiveData.value = paymentMethodsModel
    }
}
