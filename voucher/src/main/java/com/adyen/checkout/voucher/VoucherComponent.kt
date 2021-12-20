/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2021.
 */

package com.adyen.checkout.voucher

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.VoucherAction
import com.adyen.checkout.core.exception.ComponentException

class VoucherComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: VoucherConfiguration,
) : BaseActionComponent<VoucherConfiguration>(savedStateHandle, application, configuration),
    ViewableComponent<VoucherOutputData, VoucherConfiguration, ActionComponentData> {

    private val mOutputLiveData = MutableLiveData<VoucherOutputData>()
    private var url: String? = null

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<VoucherOutputData>) {
        mOutputLiveData.observe(lifecycleOwner, observer)
    }

    override fun getOutputData(): VoucherOutputData? {
        return mOutputLiveData.value
    }

    override fun sendAnalyticsEvent(context: Context) {
        // no ops
    }

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        if (action !is VoucherAction) throw ComponentException("Unsupported action")
        url = action.url

        mOutputLiveData.postValue(
            VoucherOutputData(
                true,
                action.paymentMethodType
            )
        )
    }

    fun getDownloadUrl(): String? {
        return url
    }

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<VoucherComponent, VoucherConfiguration> = VoucherComponentProvider()
    }
}
