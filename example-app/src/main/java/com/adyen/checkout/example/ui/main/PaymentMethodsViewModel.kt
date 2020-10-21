/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.example.data.api.model.paymentsRequest.PaymentMethodsRequest
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PaymentMethodsViewModel(
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage
) : ViewModel() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    private val parentJob = Job()

    private val coroutineContext: CoroutineContext get() = parentJob + Dispatchers.Main

    private val scope = CoroutineScope(coroutineContext)

    val paymentMethodResponseLiveData = MutableLiveData<PaymentMethodsApiResponse>()

    fun requestPaymentMethods() {
        scope.launch {
            paymentMethodResponseLiveData.postValue(paymentsRepository.getPaymentMethods(getPaymentMethodRequest()))
        }
    }

    private fun getPaymentMethodRequest(): PaymentMethodsRequest {
        return PaymentMethodsRequest(
            keyValueStorage.getMerchantAccount(),
            keyValueStorage.getShopperReference(),
            keyValueStorage.getAmount(),
            keyValueStorage.getCountry(),
            keyValueStorage.getShopperLocale())
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        coroutineContext.cancel()
    }
}
