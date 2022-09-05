/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */
package com.adyen.checkout.await

import android.app.Activity
import android.app.Application
import android.content.Context
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
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.AwaitAction
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
class AwaitComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: AwaitConfiguration,
    private val awaitDelegate: AwaitDelegate,
) : BaseActionComponent<AwaitConfiguration>(savedStateHandle, application, configuration),
    ViewableComponent<AwaitOutputData, AwaitConfiguration, ActionComponentData> {

    override val outputData: AwaitOutputData? get() = awaitDelegate.outputData

    init {
        awaitDelegate.initialize(viewModelScope)

        awaitDelegate.detailsFlow
            .onEach { notifyDetails(it) }
            .launchIn(viewModelScope)

        awaitDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleActionInternal(action: Action, activity: Activity) {
        if (action !is AwaitAction) {
            notifyException(ComponentException("Unsupported action"))
            return
        }
        awaitDelegate.handleAction(action, activity)
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                awaitDelegate.refreshStatus()
            }
        })
    }

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<AwaitOutputData>) {
        awaitDelegate.outputDataFlow
            .filterNotNull()
            .asLiveData()
            .observe(lifecycleOwner, observer)
    }

    override fun sendAnalyticsEvent(context: Context) = Unit

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        awaitDelegate.onCleared()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: ActionComponentProvider<AwaitComponent, AwaitConfiguration, AwaitDelegate> =
            AwaitComponentProvider()
    }
}
