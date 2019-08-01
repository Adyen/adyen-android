/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */

package com.adyen.checkout.example.arch

import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.api.CheckoutApiService
import com.adyen.checkout.example.api.model.PaymentMethodsRequest

class PaymentMethodsRepository {

    companion object {
        private val TAG: String = LogUtil.getTag()
    }

    suspend fun getPaymentMethods(): PaymentMethodsApiResponse? {
        Logger.d(TAG, "getPaymentMethods")

        val paymentMethodsRequest = PaymentMethodsRequest("TestMerchantCheckout", BuildConfig.SHOPPER_REFERENCE)
        val result = CheckoutApiService.INSTANCE.paymentMethodsAsync(paymentMethodsRequest).await()

        Logger.d(TAG, "PaymentMethodsApiResponse received - ${result.body()}")

        return result.body()

        // TODO treat connection failures
    }
}
