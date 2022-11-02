/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */
package com.adyen.checkout.adyen3ds2

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponent
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.flow.mapToCallbackWithLifeCycle
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

class Adyen3DS2Component(
    override val configuration: Adyen3DS2Configuration,
    override val delegate: Adyen3DS2Delegate,
) : ViewModel(),
    ActionComponent<Adyen3DS2Configuration>,
    IntentHandlingComponent,
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    private var observerJobs: MutableList<Job> = mutableListOf()

    override fun observe(lifecycleOwner: LifecycleOwner, callback: (ActionComponentEvent) -> Unit) {
        removeObserver()
        delegate.detailsFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope, observerJobs) {
            callback(ActionComponentEvent.ActionDetails(it))
        }

        delegate.exceptionFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope, observerJobs) {
            callback(ActionComponentEvent.Error(ComponentError(it)))
        }
    }

    /**
     * Set a [UiCustomization] object to be passed to the 3DS2 SDK for customizing the challenge screen.
     * Needs to be set before handling any action.
     *
     * @param uiCustomization The customization object.
     */
    fun setUiCustomization(uiCustomization: UiCustomization?) {
        delegate.set3DS2UICustomization(uiCustomization)
    }

    override fun handleAction(action: Action, activity: Activity) {
        delegate.handleAction(action, activity)
    }

    /**
     * Call this method when receiving the return URL from the 3DS redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        delegate.handleIntent(intent)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        delegate.onCleared()
        removeObserver()
    }

    override fun removeObserver() {
        if (observerJobs.isEmpty()) return
        Logger.d(TAG, "cleaning up existing observer")
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration, Adyen3DS2Delegate> =
            Adyen3DS2ComponentProvider()
    }
}
