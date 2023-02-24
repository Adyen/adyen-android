/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action.internal.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.IntentHandlingDelegate
import com.adyen.checkout.components.core.internal.ui.StatusPollingDelegate
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.internal.util.repeatOnResume
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import com.adyen.threeds2.customization.UiCustomization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow

@Suppress("TooManyFunctions")
internal class DefaultGenericActionDelegate(
    private val observerRepository: ActionObserverRepository,
    private val savedStateHandle: SavedStateHandle,
    private val configuration: GenericActionConfiguration,
    override val componentParams: GenericComponentParams,
    private val actionDelegateProvider: ActionDelegateProvider,
) : GenericActionDelegate {

    private var _delegate: ActionDelegate? = null
    override val delegate: ActionDelegate get() = requireNotNull(_delegate)

    private val _viewFlow = MutableStateFlow<ComponentViewType?>(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private var uiCustomization: UiCustomization? = null

    override fun initialize(coroutineScope: CoroutineScope) {
        Logger.d(TAG, "initialize")
        _coroutineScope = coroutineScope
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = detailsFlow,
            exceptionFlow = exceptionFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        )

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.repeatOnResume { refreshStatus() }
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        // This check is to support an older flow where you might need to call handleAction several times with 3DS2.
        // Initially handleAction is called with a fingerprint action then with a challenge action.
        // During this whole flow the same transaction instance should be used for both fingerprint and challenge.
        // Therefore we are making sure the same delegate persists when handleAction is called again.
        if (_delegate is Adyen3DS2Delegate && action is Threeds2ChallengeAction) {
            Logger.d(TAG, "Continuing the handling of 3ds2 challenge with old flow.")
        } else {
            val delegate = actionDelegateProvider.getDelegate(
                action = action,
                configuration = configuration,
                savedStateHandle = savedStateHandle,
                application = activity.application
            )
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

    private fun observeExceptions(delegate: ActionDelegate) {
        Logger.d(TAG, "Observing exceptions")
        delegate.exceptionFlow
            .onEach { exceptionChannel.trySend(it) }
            .launchIn(coroutineScope)
    }

    private fun observeDetails(delegate: ActionDelegate) {
        if (delegate !is DetailsEmittingDelegate) return
        Logger.d(TAG, "Observing details")
        delegate.detailsFlow
            .onEach { detailsChannel.trySend(it) }
            .launchIn(coroutineScope)
    }

    private fun observeViewFlow(delegate: ActionDelegate) {
        if (delegate !is ViewProvidingDelegate) return
        Logger.d(TAG, "Observing view flow")
        delegate.viewFlow
            .onEach { _viewFlow.tryEmit(it) }
            .launchIn(coroutineScope)
    }

    override fun set3DS2UICustomization(uiCustomization: UiCustomization?) {
        this.uiCustomization = uiCustomization
        set3DS2UICustomizationInDelegate(_delegate)
    }

    private fun set3DS2UICustomizationInDelegate(delegate: ActionDelegate?) {
        if (delegate !is Adyen3DS2Delegate) return
        if (uiCustomization != null) {
            Logger.d(TAG, "Setting UICustomization on 3DS2 delegate")
        }
        delegate.set3DS2UICustomization(uiCustomization)
    }

    override fun handleIntent(intent: Intent) {
        when (val delegate = _delegate) {
            null -> {
                exceptionChannel.trySend(ComponentException("handleIntent should not be called before handleAction"))
            }
            !is IntentHandlingDelegate -> {
                exceptionChannel.trySend(ComponentException("Cannot handle intent with the current component"))
            }
            else -> {
                Logger.d(TAG, "Handling intent")
                delegate.handleIntent(intent)
            }
        }
    }

    override fun refreshStatus() {
        val delegate = _delegate
        if (delegate !is StatusPollingDelegate) return
        Logger.d(TAG, "Refreshing status")
        delegate.refreshStatus()
    }

    override fun onError(e: CheckoutException) {
        delegate.onError(e)
    }

    override fun onCleared() {
        Logger.d(TAG, "onCleared")
        removeObserver()
        _delegate?.onCleared()
        _delegate = null
        _coroutineScope = null
        uiCustomization = null
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
