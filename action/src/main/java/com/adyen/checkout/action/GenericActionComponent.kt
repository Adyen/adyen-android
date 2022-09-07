/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */
package com.adyen.checkout.action

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
import com.adyen.checkout.adyen3ds2.Adyen3DS2Delegate
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.base.IntentHandlingDelegate
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.base.StatusPollingDelegate
import com.adyen.checkout.components.base.ViewableDelegate
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.status.model.TimerData
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
class GenericActionComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: GenericActionConfiguration,
) : BaseActionComponent<GenericActionConfiguration>(savedStateHandle, application, configuration),
    ViewableComponent<OutputData, GenericActionConfiguration, ActionComponentData>,
    IntentHandlingComponent {

    private var _delegate: ActionDelegate<Action>? = null
    private val delegate: ActionDelegate<Action> get() = requireNotNull(_delegate)

    override val outputData: OutputData?
        get() = (delegate as? ViewableDelegate<*>)?.outputData

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleActionInternal(action: Action, activity: Activity) {
        if (_delegate is Adyen3DS2Delegate && action is Threeds2ChallengeAction) {
            Logger.d(TAG, "Continuing the handling of 3ds2 challenge with old flow.")
        } else {
            val delegate = ActionDelegateProvider.get(action, configuration, savedStateHandle, activity.application)
            _delegate = delegate

            delegate.initialize(viewModelScope)

            observeDetails()
            observeExceptions()
        }

        delegate.handleAction(action, activity)
    }

    private fun observeExceptions() {
        delegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    private fun observeDetails() {
        (delegate as? DetailsEmittingDelegate)?.detailsFlow
            ?.onEach { notifyDetails(it) }
            ?.launchIn(viewModelScope)
    }

    override fun observeOutputData(lifecycleOwner: LifecycleOwner, observer: Observer<OutputData>) {
        (delegate as? ViewableDelegate<*>)?.outputDataFlow
            ?.filterNotNull()
            ?.asLiveData()
            ?.observe(lifecycleOwner, observer)
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)

        (delegate as? StatusPollingDelegate)?.let { statusPollingDelegate ->
            // Immediately request a new status if the user resumes the app
            lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    statusPollingDelegate.refreshStatus()
                }
            })
        }
    }

    fun observeTimer(lifecycleOwner: LifecycleOwner, observer: Observer<TimerData>) {
        (delegate as? StatusPollingDelegate)?.timerFlow
            ?.asLiveData()
            ?.observe(lifecycleOwner, observer)
    }

    fun set3DS2UICustomization(uiCustomization: UiCustomization?) {
        (delegate as? Adyen3DS2Delegate)?.set3DS2UICustomization(uiCustomization)
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        (delegate as? IntentHandlingDelegate)?.handleIntent(intent)
    }

    override fun sendAnalyticsEvent(context: Context) = Unit

    override fun onCleared() {
        super.onCleared()
        delegate.onCleared()
        _delegate = null
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: ActionComponentProvider<GenericActionComponent, GenericActionConfiguration, ActionDelegate<*>> =
            GenericActionComponentProvider()
    }
}
