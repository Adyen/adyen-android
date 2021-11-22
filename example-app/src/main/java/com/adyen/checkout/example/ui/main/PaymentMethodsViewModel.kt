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
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.adyen.checkout.example.service.getPaymentMethodRequest
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
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
            paymentMethodResponseLiveData.postValue(
                paymentsRepository.getPaymentMethods(
                    getPaymentMethodRequest(keyValueStorage)
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        coroutineContext.cancel()
    }
}
