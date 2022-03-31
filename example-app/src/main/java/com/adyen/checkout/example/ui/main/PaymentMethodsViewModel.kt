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
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.paymentMethods.PaymentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.adyen.checkout.example.service.getPaymentMethodRequest
import kotlinx.coroutines.launch

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val keyValueStorage: KeyValueStorage
) : ViewModel() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    val paymentMethodResponseLiveData = MutableLiveData<PaymentMethodsApiResponse>()

    fun requestPaymentMethods() {
        viewModelScope.launch {
            paymentMethodResponseLiveData.postValue(
                paymentsRepository.getPaymentMethods(
                    getPaymentMethodRequest(
                        merchantAccount = keyValueStorage.getMerchantAccount(),
                        shopperReference = keyValueStorage.getShopperReference(),
                        amount = keyValueStorage.getAmount(),
                        countryCode = keyValueStorage.getCountry(),
                        shopperLocale = keyValueStorage.getShopperLocale(),
                        splitCardFundingSources = keyValueStorage.isSplitCardFundingSources()
                    )
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
    }
}
