/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */
package com.adyen.checkout.components.base

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.base.lifecycle.ActionComponentViewModel
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil

@Suppress("TooManyFunctions")
abstract class BaseActionComponent<ConfigurationT : Configuration>(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: ConfigurationT
) : ActionComponentViewModel<ConfigurationT>(savedStateHandle, application, configuration) {

    private val resultLiveData = MutableLiveData<ActionComponentData>()
    private val errorMutableLiveData = MutableLiveData<ComponentError>()

    override fun handleAction(activity: Activity, action: Action) {
        if (!canHandleAction(action)) {
            notifyException(ComponentException("Action type not supported by this component - " + action.type))
            return
        }
        try {
            handleActionInternal(action, activity)
        } catch (e: ComponentException) {
            notifyException(e)
        }
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        resultLiveData.observe(lifecycleOwner, observer)
    }

    override fun removeObservers(lifecycleOwner: LifecycleOwner) {
        resultLiveData.removeObservers(lifecycleOwner)
    }

    override fun removeObserver(observer: Observer<ActionComponentData>) {
        resultLiveData.removeObserver(observer)
    }

    override fun observeErrors(lifecycleOwner: LifecycleOwner, observer: Observer<ComponentError>) {
        errorMutableLiveData.observe(lifecycleOwner, observer)
    }

    override fun removeErrorObservers(lifecycleOwner: LifecycleOwner) {
        errorMutableLiveData.removeObservers(lifecycleOwner)
    }

    override fun removeErrorObserver(observer: Observer<ComponentError>) {
        errorMutableLiveData.removeObserver(observer)
    }

    @Throws(ComponentException::class)
    protected abstract fun handleActionInternal(action: Action, activity: Activity)

    @Throws(ComponentException::class)
    protected fun notifyDetails(actionComponentData: ActionComponentData) {
        resultLiveData.postValue(actionComponentData)
    }

    protected fun notifyException(e: CheckoutException) {
        errorMutableLiveData.postValue(ComponentError(e))
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
