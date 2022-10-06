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
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ViewableComponent
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.ui.ViewProvidingComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.log.LogUtil
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
class GenericActionComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: GenericActionConfiguration,
    private val genericActionDelegate: GenericActionDelegate,
) : BaseActionComponent<GenericActionConfiguration>(savedStateHandle, application, configuration),
    ViewableComponent<GenericActionConfiguration, ActionComponentData>,
    IntentHandlingComponent,
    ViewProvidingComponent {

    override val delegate: ActionDelegate<Action>
        get() = genericActionDelegate.delegate

    override val viewFlow: Flow<ComponentViewType?>
        get() = genericActionDelegate.viewFlow

    init {
        genericActionDelegate.initialize(viewModelScope)

        genericActionDelegate.detailsFlow
            .onEach { notifyDetails(it) }
            .launchIn(viewModelScope)

        genericActionDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleActionInternal(action: Action, activity: Activity) {
        genericActionDelegate.handleAction(action, activity)
    }

    override fun observe(lifecycleOwner: LifecycleOwner, observer: Observer<ActionComponentData>) {
        super.observe(lifecycleOwner, observer)

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                genericActionDelegate.refreshStatus()
            }
        })
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

    override fun sendAnalyticsEvent(context: Context) = Unit

    override fun onCleared() {
        super.onCleared()
        genericActionDelegate.onCleared()
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
