/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 1/5/2019.
 */

package com.adyen.checkout.dropin.activity

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.drawable.BitmapDrawable
import com.adyen.checkout.base.api.LogoApi
import com.adyen.checkout.base.api.LogoConnectionTask
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn

class PaymentMethodPickerViewModel : ViewModel() {

    companion object {
        val TAG = LogUtil.getTag()

        private const val CARD_LOGO_TYPE = "card"
    }

    val paymentMethodsModelLiveData: MutableLiveData<List<PaymentMethodModel>> = MutableLiveData()

    var paymentMethodsApiResponse: PaymentMethodsApiResponse = PaymentMethodsApiResponse()
        set(value) {
            Logger.d(TAG, "set")
            if (value != paymentMethodsApiResponse) {
                field = value
                if (value.paymentMethods != null) {
                    onPaymentMethodsChanged(value.paymentMethods!!)
                }
            }
        }

    private fun onPaymentMethodsChanged(paymentMethodsList: List<PaymentMethod>) {
        val modelList = ArrayList<PaymentMethodModel>()
        val config = DropIn.INSTANCE.configuration
        val logoApi = LogoApi.getInstance(config.environment, config.displayMetrics)

        for (paymentMethod in paymentMethodsList) {
            if (paymentMethod.type == null) {
                Logger.e(TAG, "Unexpected null type on PaymentMethod")
                continue
            }

            // If details is empty we default back to redirect, otherwise we don't support it.
            if (!PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethod.type) && paymentMethod.details != null) {
                Logger.e(TAG, "PaymentMethod not yet supported - ${paymentMethod.type}")
                continue
            }

            if (paymentMethod.type == PaymentMethodTypes.SCHEME &&
                    DropIn.INSTANCE.configuration.getConfigurationFor<CardConfiguration>(PaymentMethodTypes.SCHEME) == null) {
                Logger.e(TAG, "WARNING!")
                Logger.e(TAG, "Configuration object is required to make Credit Card payments!")
                continue
            }

            val pmModel = PaymentMethodModel(paymentMethod, null)
            modelList.add(pmModel)

            val callback = object : LogoConnectionTask.LogoCallback {
                override fun onLogoReceived(drawable: BitmapDrawable) {
                    Logger.d(TAG, "onLogoReceived")
                    pmModel.logo = drawable
                    // notify data changed
                    paymentMethodsModelLiveData.value = paymentMethodsModelLiveData.value
                }

                override fun onReceiveFailed() {
                    Logger.e(TAG, "Logo receive failed for ${paymentMethod.type}")
                }
            }

            // Credit card logo is called card instead of scheme
            if (PaymentMethodTypes.SCHEME == paymentMethod.type) {
                logoApi.getLogo(CARD_LOGO_TYPE, null, null, callback)
            } else {
                logoApi.getLogo(paymentMethod.type!!, null, null, callback)
            }
        }
        paymentMethodsModelLiveData.value = modelList
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        val config = DropIn.INSTANCE.configuration
        LogoApi.getInstance(config.environment, config.displayMetrics).cancellAll()
    }
}
