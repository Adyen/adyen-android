/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.components.base

import android.content.Context
import android.text.TextUtils
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.analytics.AnalyticEvent.Companion.create
import com.adyen.checkout.components.analytics.AnalyticEvent.Flavor
import com.adyen.checkout.components.analytics.AnalyticsDispatcher.Companion.dispatchEvent
import com.adyen.checkout.components.base.lifecycle.PaymentComponentViewModel
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.api.ThreadManager
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger

/**
 * Component should not be instantiated directly. Instead use the PROVIDER object.
 *
 * @param savedStateHandle      [SavedStateHandle]
 * @param paymentMethodDelegate [PaymentMethodDelegate]
 * @param configuration         [ConfigurationT]
 */
@Suppress("TooManyFunctions")
abstract class BasePaymentComponent<
    ConfigurationT : Configuration,
    InputDataT : InputData,
    OutputDataT : OutputData,
    ComponentStateT : PaymentComponentState<out PaymentMethodDetails>
    >(
    savedStateHandle: SavedStateHandle,
    paymentMethodDelegate: PaymentMethodDelegate,
    configuration: ConfigurationT
) : PaymentComponentViewModel<ConfigurationT, ComponentStateT>(savedStateHandle, paymentMethodDelegate, configuration),
    ViewableComponent<OutputDataT, ConfigurationT, ComponentStateT> {

    protected var latestInputData: InputDataT? = null
    private val paymentComponentStateLiveData = MutableLiveData<ComponentStateT>()
    private val componentErrorLiveData = MutableLiveData<ComponentError>()
    private val outputLiveData = MutableLiveData<OutputDataT>()
    private var isCreatedForDropIn = false
    private var isAnalyticsEnabled = true

    init {
        assertSupported(paymentMethodDelegate.getPaymentMethodType())
    }

    override fun requiresInput(): Boolean {
        // By default all components require user input.
        return true
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ComponentStateT>) {
        paymentComponentStateLiveData.observe(lifecycleOwner, observer)
    }

    override fun removeObservers(lifecycleOwner: LifecycleOwner) {
        paymentComponentStateLiveData.removeObservers(lifecycleOwner)
    }

    override fun removeObserver(observer: Observer<ComponentStateT>) {
        paymentComponentStateLiveData.removeObserver(observer)
    }

    override fun observeErrors(lifecycleOwner: LifecycleOwner, observer: Observer<ComponentError>) {
        componentErrorLiveData.observe(lifecycleOwner, observer)
    }

    override fun removeErrorObservers(lifecycleOwner: LifecycleOwner) {
        componentErrorLiveData.removeObservers(lifecycleOwner)
    }

    override fun removeErrorObserver(observer: Observer<ComponentError>) {
        componentErrorLiveData.removeObserver(observer)
    }

    override val state: ComponentStateT?
        get() = paymentComponentStateLiveData.value

    /**
     * Receives a set of [InputData] from the user to be processed.
     *
     * @param inputData [InputDataT]
     */
    fun inputDataChanged(inputData: InputDataT) {
        Logger.v(TAG, "inputDataChanged")
        latestInputData = inputData
        notifyStateChanged(onInputDataChanged(inputData))
    }

    /**
     * Sets if the analytics events can be sent by the component.
     * Default is True.
     *
     * @param isEnabled Is analytics should be enabled or not.
     */
    // TODO: 13/11/2020 Add to Configuration instead?
    fun setAnalyticsEnabled(isEnabled: Boolean) {
        isAnalyticsEnabled = isEnabled
    }

    /**
     * Send an analytic event about the Component being shown to the user.
     *
     * @param context The context where the component is.
     */
    override fun sendAnalyticsEvent(context: Context) {
        if (isAnalyticsEnabled) {
            val flavor: Flavor = if (isCreatedForDropIn) {
                Flavor.DROPIN
            } else {
                Flavor.COMPONENT
            }
            val type = paymentMethodDelegate.getPaymentMethodType()
            if (TextUtils.isEmpty(type)) {
                throw CheckoutException("Payment method has empty or null type")
            }
            val analyticEvent = create(context, flavor, type, configuration.shopperLocale)
            dispatchEvent(context, configuration.environment, analyticEvent)
        }
    }

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<OutputDataT>) {
        // Parent component needs to overrides this for view to have access to the method in the package
        outputLiveData.observe(lifecycleOwner, observer)
    }

    override val outputData: OutputDataT?
        get() = outputLiveData.value

    /**
     * Called every time the [InputData] changes.
     *
     * @param inputData The new InputData
     * @return The OutputData after processing.
     */
    protected abstract fun onInputDataChanged(inputData: InputDataT): OutputDataT
    @WorkerThread
    protected abstract fun createComponentState(): ComponentStateT
    protected fun notifyException(e: CheckoutException) {
        Logger.e(TAG, "notifyException - " + e.message)
        componentErrorLiveData.postValue(ComponentError(e))
    }

    /**
     * Indicates that the output data has changed and the component should recreate its state
     * and notify its observers.
     *
     * @param outputData the new output data
     */
    protected fun notifyStateChanged(outputData: OutputDataT) {
        Logger.d(TAG, "notifyStateChanged with OutputData")
        if (outputData != outputLiveData.value) {
            outputLiveData.value = outputData
            notifyStateChanged()
        } else {
            Logger.d(TAG, "state has not changed")
        }
    }

    /**
     * Asks the component to recreate its state and notify its observers.
     */
    @Suppress("TooGenericExceptionCaught")
    protected fun notifyStateChanged() {
        Logger.d(TAG, "notifyStateChanged")
        ThreadManager.EXECUTOR.submit {
            try {
                paymentComponentStateLiveData.postValue(createComponentState())
            } catch (e: Exception) {
                Logger.e(TAG, "notifyStateChanged - error:" + e.message)
                notifyException(ComponentException("Unexpected error", e))
            }
        }
    }

    private fun assertSupported(paymentMethodType: String) {
        require(isSupported(paymentMethodType)) { "Unsupported payment method type $paymentMethodType" }
    }

    private fun isSupported(paymentMethodType: String): Boolean {
        for (supportedType in supportedPaymentMethodTypes) {
            if (supportedType == paymentMethodType) {
                return true
            }
        }
        return false
    }

    fun setCreatedForDropIn() {
        isCreatedForDropIn = true
    }

    companion object {
        private val TAG = getTag()
    }
}
