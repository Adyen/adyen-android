/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/1/2021.
 */

package com.adyen.checkout.dropin

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.service.DropInServiceResult
import org.json.JSONObject

val TAG = LogUtil.getTag()

internal object DropInHandler {

    private val paymentsLiveData = MutableLiveData<JSONObject>()
    private val detailsLiveData = MutableLiveData<JSONObject>()
    private val resultLiveData = MutableLiveData<DropInServiceResult>()

    init {
        Logger.d(TAG, "initialized")
    }

    fun observeDropIn(observer: DropInObserver) {
        paymentsLiveData.observe(observer) {
            if (it != null) observer.makePaymentsCall(it)
        }
        detailsLiveData.observe(observer) {
            if (it != null) observer.makeDetailsCall(it)
        }
    }

    fun sendResult(result: DropInServiceResult) {
        resultLiveData.value = result
    }

    fun provideDropIn(provider: DropInProvider) {
        resultLiveData.observe(provider) {
            if (it != null) provider.observeDropInServiceResult(it)
        }
    }

    fun resetDropIn() {
        paymentsLiveData.value = null
        detailsLiveData.value = null
        resultLiveData.value = null
    }

    fun requestPaymentsCall(paymentComponentData: JSONObject) {
        paymentsLiveData.value = paymentComponentData
    }

    fun requestDetailsCall(actionComponentData: JSONObject) {
        detailsLiveData.value = actionComponentData
    }

}

interface DropInObserver : LifecycleOwner {
    fun makePaymentsCall(paymentComponentData: JSONObject)
    fun makeDetailsCall(actionComponentData: JSONObject)
}

internal interface DropInProvider : LifecycleOwner {
    fun observeDropInServiceResult(result: DropInServiceResult)
}