/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.Adyen3DS2Delegate
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.IntentHandlingDelegate
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.base.StatusPollingDelegate
import com.adyen.checkout.components.base.ViewableDelegate
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.Threeds2ChallengeAction
import com.adyen.checkout.components.ui.ViewProvider
import com.adyen.checkout.components.ui.ViewProvidingDelegate
import com.adyen.checkout.components.ui.view.ComponentViewType
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class DefaultGenericActionDelegate(
    private val savedStateHandle: SavedStateHandle,
    private val configuration: GenericActionConfiguration,
    private val actionDelegateProvider: ActionDelegateProvider,
) : GenericActionDelegate {
    private var _delegate: ActionDelegate<Action>? = null
    override val delegate: ActionDelegate<Action> get() = requireNotNull(_delegate)

    override val outputData: OutputData?
        get() = (_delegate as? ViewableDelegate<*>)?.outputData

    private val _viewFlow = MutableStateFlow<ComponentViewType?>(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private val _detailsFlow: MutableSharedFlow<ActionComponentData> = MutableSingleEventSharedFlow()
    override val detailsFlow: Flow<ActionComponentData> = _detailsFlow

    private var uiCustomization: UiCustomization? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
    }

    override fun handleAction(action: Action, activity: Activity) {
        // This check is to support an older flow where you might need to call handleAction several times with 3DS2.
        // Initially handleAction is called with a fingerprint action then with a challenge action.
        // During this whole flow the same transaction instance should be used for both fingerprint and challenge.
        // Therefore we are making sure the same delegate persists when handleAction is called again.
        if (_delegate is Adyen3DS2Delegate && action is Threeds2ChallengeAction) {
            Logger.d(TAG, "Continuing the handling of 3ds2 challenge with old flow.")
        } else {
            val delegate = actionDelegateProvider.get(action, configuration, savedStateHandle, activity.application)
            this._delegate = delegate
            Logger.d(TAG, "Created delegate of type ${delegate::class.simpleName}")

            delegate.initialize(coroutineScope)

            set3DS2UICustomizationInDelegate(delegate)

            observeDetails(delegate)
            observeExceptions(delegate)
            observeViewFlow(delegate)
        }

        delegate.handleAction(action, activity)
    }

    private fun observeExceptions(delegate: ActionDelegate<Action>) {
        delegate.exceptionFlow
            .onEach { _exceptionFlow.tryEmit(it) }
            .launchIn(coroutineScope)
    }

    private fun observeDetails(delegate: ActionDelegate<Action>) {
        if (delegate is DetailsEmittingDelegate) {
            Logger.d(TAG, "Observing details")
            delegate.detailsFlow
                .onEach { _detailsFlow.tryEmit(it) }
                .launchIn(coroutineScope)
        }
    }

    private fun observeViewFlow(delegate: ActionDelegate<Action>) {
        if (delegate is ViewProvidingDelegate) {
            Logger.d(TAG, "Observing view flow")
            delegate.viewFlow
                .onEach { _viewFlow.tryEmit(it) }
                .launchIn(coroutineScope)
        }
    }

    override fun set3DS2UICustomization(uiCustomization: UiCustomization?) {
        this.uiCustomization = uiCustomization
        set3DS2UICustomizationInDelegate(_delegate)
    }

    private fun set3DS2UICustomizationInDelegate(delegate: ActionDelegate<Action>?) {
        if (delegate is Adyen3DS2Delegate) {
            if (uiCustomization != null) {
                Logger.d(TAG, "Setting UICustomization on 3DS2 delegate")
            }
            delegate.set3DS2UICustomization(uiCustomization)
        }
    }

    override fun handleIntent(intent: Intent) {
        if (_delegate == null) {
            _exceptionFlow.tryEmit(ComponentException("handleIntent should not be called before handleAction"))
            return
        }
        (delegate as? IntentHandlingDelegate)?.let {
            Logger.d(TAG, "Handling intent")
            it.handleIntent(intent)
        }
    }

    override fun refreshStatus() {
        (_delegate as? StatusPollingDelegate)?.let {
            Logger.d(TAG, "Refreshing status")
            it.refreshStatus()
        }
    }

    override fun getViewProvider(): ViewProvider =
        throw IllegalStateException("GenericActionDelegate doesn't have a ViewProvider")

    override fun onCleared() {
        _delegate?.onCleared()
        _delegate = null
        _coroutineScope = null
        uiCustomization = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
