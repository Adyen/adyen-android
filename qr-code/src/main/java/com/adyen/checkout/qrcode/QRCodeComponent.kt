/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/4/2021.
 */
package com.adyen.checkout.qrcode

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.ActionComponentProviderImpl
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

private val TAG = LogUtil.getTag()
private val ACTION_TYPES = listOf(QrCodeAction.ACTION_TYPE)

class QRCodeComponent(application: Application, configuration: QRCodeConfiguration) :
    BaseActionComponent<QRCodeConfiguration>(application, configuration),
    ViewableComponent<QRCodeOutputData, QRCodeConfiguration, ActionComponentData> {

    private val mOutputLiveData = MutableLiveData<QRCodeOutputData>()
    private var mPaymentMethodType: String? = null

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        val configuration = configuration
            ?: throw ComponentException("Configuration not found")
        mPaymentMethodType = action.paymentMethodType
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
    }

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<QRCodeOutputData>) {
        mOutputLiveData.observe(lifecycleOwner, observer)
    }

    override fun getOutputData(): QRCodeOutputData? {
        return mOutputLiveData.value
    }

    override fun sendAnalyticsEvent(context: Context) {
        // TODO: 28/08/2020 Do we have an event for this?
    }

    override fun getSupportedActionTypes(): List<String> = ACTION_TYPES

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<QRCodeComponent> = ActionComponentProviderImpl(QRCodeComponent::class.java, QRCodeConfiguration::class.java, true)
    }
}
