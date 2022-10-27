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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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

    @Throws(ComponentException::class)
    protected abstract fun handleActionInternal(action: Action, activity: Activity)

    protected fun notifyException(e: CheckoutException) {
        errorMutableLiveData.postValue(ComponentError(e))
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
