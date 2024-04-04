/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/9/2022.
 */

package com.adyen.checkout.action.core.internal.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.adyen3ds2.internal.ui.Adyen3DS2Delegate
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.Threeds2ChallengeAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PermissionRequestData
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.IntentHandlingDelegate
import com.adyen.checkout.components.core.internal.ui.PermissionRequestingDelegate
import com.adyen.checkout.components.core.internal.ui.RedirectableDelegate
import com.adyen.checkout.components.core.internal.ui.StatusPollingDelegate
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.components.core.internal.util.repeatOnResume
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.core.internal.util.runCompileOnly
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
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
    override val savedStateHandle: SavedStateHandle,
    private val checkoutConfiguration: CheckoutConfiguration,
    override val componentParams: GenericComponentParams,
    private val actionDelegateProvider: ActionDelegateProvider,
    private val application: Application,
) : GenericActionDelegate, SavedStateHandleContainer {

    private var _delegate: ActionDelegate? = null
    override val delegate: ActionDelegate get() = requireNotNull(_delegate)

    private val _viewFlow = MutableStateFlow<ComponentViewType?>(null)
    override val viewFlow: Flow<ComponentViewType?> = _viewFlow

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    private val permissionChannel: Channel<PermissionRequestData> = bufferedChannel()
    override val permissionFlow: Flow<PermissionRequestData> = permissionChannel.receiveAsFlow()

    private var onRedirectListener: (() -> Unit)? = null

    private var action: Action? by SavedStateHandleProperty(ACTION_KEY)

    override fun initialize(coroutineScope: CoroutineScope) {
        adyenLog(AdyenLogLevel.DEBUG) { "initialize" }
        _coroutineScope = coroutineScope
        restoreState()
    }

    private fun restoreState() {
        adyenLog(AdyenLogLevel.DEBUG) { "Restoring state" }
        val action: Action? = action
        if (_delegate == null && action != null) {
            createDelegateAndObserve(action)
        }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = detailsFlow,
            exceptionFlow = exceptionFlow,
            permissionFlow = permissionFlow,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )

        // Immediately request a new status if the user resumes the app
        lifecycleOwner.repeatOnResume { refreshStatus() }
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        this.action = action

        // This check is to support an older flow where you might need to call handleAction several times with 3DS2.
        // Initially handleAction is called with a fingerprint action then with a challenge action.
        // During this whole flow the same transaction instance should be used for both fingerprint and challenge.
        // Therefore we are making sure the same delegate persists when handleAction is called again.
        if (isOld3DS2Flow(action)) {
            adyenLog(AdyenLogLevel.DEBUG) { "Continuing the handling of 3ds2 challenge with old flow." }
        } else {
            createDelegateAndObserve(action)
        }

        delegate.handleAction(action, activity)
    }

    private fun createDelegateAndObserve(action: Action) {
        val delegate = actionDelegateProvider.getDelegate(
            action = action,
            checkoutConfiguration = checkoutConfiguration,
            savedStateHandle = savedStateHandle,
            application = application,
        )
        _delegate = delegate
        adyenLog(AdyenLogLevel.DEBUG) { "Created delegate of type ${delegate::class.simpleName}" }

        if (delegate is RedirectableDelegate) {
            onRedirectListener?.let { delegate.setOnRedirectListener(it) }
        }

        delegate.initialize(coroutineScope)

        observeDelegate(delegate)
    }

    private fun observeDelegate(delegate: ActionDelegate) {
        observeDetails(delegate)
        observeExceptions(delegate)
        observePermissionRequests(delegate)
        observeViewFlow(delegate)
    }

    private fun isOld3DS2Flow(action: Action): Boolean {
        return runCompileOnly { _delegate is Adyen3DS2Delegate && action is Threeds2ChallengeAction } ?: false
    }

    private fun observeDetails(delegate: ActionDelegate) {
        if (delegate !is DetailsEmittingDelegate) return
        adyenLog(AdyenLogLevel.DEBUG) { "Observing details" }
        delegate.detailsFlow
            .onEach { detailsChannel.trySend(it) }
            .launchIn(coroutineScope)
    }

    private fun observeExceptions(delegate: ActionDelegate) {
        adyenLog(AdyenLogLevel.DEBUG) { "Observing exceptions" }
        delegate.exceptionFlow
            .onEach { exceptionChannel.trySend(it) }
            .launchIn(coroutineScope)
    }

    private fun observePermissionRequests(delegate: ActionDelegate) {
        if (delegate !is PermissionRequestingDelegate) return
        adyenLog(AdyenLogLevel.DEBUG) { "Observing permission requests" }
        delegate.permissionFlow
            .onEach { permissionChannel.trySend(it) }
            .launchIn(coroutineScope)
    }

    private fun observeViewFlow(delegate: ActionDelegate) {
        if (delegate !is ViewProvidingDelegate) return
        adyenLog(AdyenLogLevel.DEBUG) { "Observing view flow" }
        delegate.viewFlow
            .onEach { _viewFlow.tryEmit(it) }
            .launchIn(coroutineScope)
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
                adyenLog(AdyenLogLevel.DEBUG) { "Handling intent" }
                delegate.handleIntent(intent)
            }
        }
    }

    override fun refreshStatus() {
        val delegate = _delegate
        if (delegate !is StatusPollingDelegate) return
        adyenLog(AdyenLogLevel.DEBUG) { "Refreshing status" }
        delegate.refreshStatus()
    }

    override fun onError(e: CheckoutException) {
        delegate.onError(e)
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        _delegate?.let { delegate ->
            if (delegate is RedirectableDelegate) {
                delegate.setOnRedirectListener(listener)
            }
        }
        onRedirectListener = listener
    }

    override fun onCleared() {
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        removeObserver()
        _delegate?.onCleared()
        _delegate = null
        _coroutineScope = null
        onRedirectListener = null
    }

    companion object {
        private const val ACTION_KEY = "ACTION_KEY"
    }
}
