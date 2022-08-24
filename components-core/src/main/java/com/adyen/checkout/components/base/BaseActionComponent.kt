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
import android.os.Bundle
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
import com.adyen.checkout.core.log.Logger
import org.json.JSONObject

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
        paymentData = action.paymentData
        try {
            handleActionInternal(action, activity, paymentData)
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

    /**
     * Call this method to save the current data of the Component to the Bundle from [Activity.onSaveInstanceState].
     *
     * @param bundle The bundle to save the sate into.
     */
    @Deprecated("You can safely remove this method, we rely on {@link SavedStateHandle} to handle the state.")
    fun saveState(bundle: Bundle?) {
        Logger.w(TAG, "Calling saveState is not necessary anymore, you can safely remove this method.")
    }

    /**
     * Call this method to restore the current data of the Component from the savedInstanceState Bundle from [Activity.onCreate]}.
     *
     * @param bundle The bundle to restore the sate from.
     */
    @Deprecated("You can safely remove this method, we rely on {@link SavedStateHandle} to handle the state.")
    fun restoreState(bundle: Bundle?) {
        Logger.w(TAG, "Calling restoreState is not necessary anymore, you can safely remove this method.")
    }

    @Throws(ComponentException::class)
    protected abstract fun handleActionInternal(action: Action, activity: Activity, paymentData: String?)

    @Throws(ComponentException::class)
    protected fun notifyDetails(details: JSONObject) {
        val actionComponentData = ActionComponentData()
        actionComponentData.details = details
        actionComponentData.paymentData = paymentData
        resultLiveData.value = actionComponentData
    }

    protected fun notifyException(e: CheckoutException) {
        errorMutableLiveData.postValue(ComponentError(e))
    }

    protected var paymentData: String?
        get() = savedStateHandle.get(PAYMENT_DATA_KEY)
        protected set(paymentData) {
            savedStateHandle.set(PAYMENT_DATA_KEY, paymentData)
        }

    companion object {
        private val TAG = LogUtil.getTag()
        private const val PAYMENT_DATA_KEY = "payment_data"
    }
}
