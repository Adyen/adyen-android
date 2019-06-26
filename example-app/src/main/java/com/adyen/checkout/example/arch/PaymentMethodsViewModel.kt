/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */

package com.adyen.checkout.example.arch

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.* // ktlint-disable no-wildcard-imports

class PaymentMethodsViewModel : ViewModel() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    val paymentMethodResponseLiveData = MutableLiveData<PaymentMethodsApiResponse>()

    private val paymentMethodsRepository = PaymentMethodsRepository()
    private var myJob: Job? = null

    init {
        Logger.d(TAG, "Initiating IO Job")
        myJob = CoroutineScope(Dispatchers.IO).launch {

            val result = paymentMethodsRepository.getPaymentMethods()

            withContext(Dispatchers.Main) {
                Logger.d(TAG, "Dispatching result - $result")
                paymentMethodResponseLiveData.value = result
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        myJob?.cancel()
    }
}
