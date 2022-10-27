/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */
package com.adyen.checkout.adyen3ds2

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.ComponentResult
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.BaseThreeds2Action
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class Adyen3DS2Component(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: Adyen3DS2Configuration,
    override val delegate: Adyen3DS2Delegate,
) : BaseActionComponent<Adyen3DS2Configuration>(savedStateHandle, application, configuration),
    IntentHandlingComponent,
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    override fun observe(lifecycleOwner: LifecycleOwner, callback: (ComponentResult) -> Unit) {
        delegate.detailsFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { callback(ComponentResult.ActionDetails(it)) }
            .launchIn(viewModelScope)

        delegate.exceptionFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { callback(ComponentResult.Error(ComponentError(it))) }
            .launchIn(viewModelScope)
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

    @Throws(ComponentException::class)
    override fun handleActionInternal(action: Action, activity: Activity) {
        if (action !is BaseThreeds2Action) {
            notifyException(ComponentException("Unsupported action"))
            return
        }
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
    }

    companion object {
        private val TAG = LogUtil.getTag()

        @JvmField
        val PROVIDER: ActionComponentProvider<Adyen3DS2Component, Adyen3DS2Configuration, Adyen3DS2Delegate> =
            Adyen3DS2ComponentProvider()
    }
}
