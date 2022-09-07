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
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.QrCodeAction
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class QRCodeComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: QRCodeConfiguration,
    private val qrCodeDelegate: QRCodeDelegate,
) :
    BaseActionComponent<QRCodeConfiguration>(savedStateHandle, application, configuration),
    ViewableComponent<QRCodeOutputData, QRCodeConfiguration, ActionComponentData>,
    IntentHandlingComponent {

    override val outputData: QRCodeOutputData? get() = qrCodeDelegate.outputData

    init {
        qrCodeDelegate.initialize(viewModelScope)

        qrCodeDelegate.detailsFlow
            .onEach { notifyDetails(it) }
            .launchIn(viewModelScope)

        qrCodeDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleActionInternal(action: Action, activity: Activity) {
        if (action !is QrCodeAction) {
            notifyException(ComponentException("Unsupported action"))
            return
        }
        qrCodeDelegate.handleAction(action, activity)
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        qrCodeDelegate.handleIntent(intent)
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                qrCodeDelegate.refreshStatus()
            }
        })
    }

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<QRCodeOutputData>) {
        qrCodeDelegate.outputDataFlow
            .filterNotNull()
            .asLiveData()
            .observe(lifecycleOwner, observer)
    }

    fun observeTimer(lifecycleOwner: LifecycleOwner, observer: Observer<TimerData>) {
        qrCodeDelegate.timerFlow
            .asLiveData()
            .observe(lifecycleOwner, observer)
    }

    override fun sendAnalyticsEvent(context: Context) = Unit

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        qrCodeDelegate.onCleared()
    }

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<QRCodeComponent, QRCodeConfiguration, QRCodeDelegate> =
            QRCodeComponentProvider()

        private val TAG = LogUtil.getTag()
    }
}
