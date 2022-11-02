/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/8/2022.
 */
package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponent
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.base.ActionDelegate
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

@Suppress("TooManyFunctions")
class GenericActionComponent(
    override val configuration: GenericActionConfiguration,
    private val genericActionDelegate: GenericActionDelegate,
) : ViewModel(),
    ActionComponent<GenericActionConfiguration>,
    ViewableComponent,
    IntentHandlingComponent {

    override val delegate: ActionDelegate
        get() = genericActionDelegate.delegate

    override val viewFlow: Flow<ComponentViewType?>
        get() = genericActionDelegate.viewFlow

    init {
        genericActionDelegate.initialize(viewModelScope)
    }

    private var observerJobs: MutableList<Job> = mutableListOf()

    override fun observe(lifecycleOwner: LifecycleOwner, callback: (ActionComponentEvent) -> Unit) {
        removeObserver()
        genericActionDelegate.detailsFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope, observerJobs) {
            callback(ActionComponentEvent.ActionDetails(it))
        }

        genericActionDelegate.exceptionFlow.mapToCallbackWithLifeCycle(lifecycleOwner, viewModelScope, observerJobs) {
            callback(ActionComponentEvent.Error(ComponentError(it)))
        }

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                genericActionDelegate.refreshStatus()
            }
        })
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        genericActionDelegate.handleAction(action, activity)
    }

    fun set3DS2UICustomization(uiCustomization: UiCustomization?) {
        genericActionDelegate.set3DS2UICustomization(uiCustomization)
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        genericActionDelegate.handleIntent(intent)
    }

    override fun onCleared() {
        super.onCleared()
        Logger.d(TAG, "onCleared")
        genericActionDelegate.onCleared()
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
        val PROVIDER: ActionComponentProvider<
            GenericActionComponent,
            GenericActionConfiguration,
            GenericActionDelegate
            > = GenericActionComponentProvider()
    }
}
