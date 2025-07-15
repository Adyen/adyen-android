/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/8/2022.
 */

package com.adyen.checkout.redirect.internal.ui

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.ActionTypes
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.exception.ComponentException
import com.adyen.checkout.core.old.exception.HttpException
import com.adyen.checkout.core.old.exception.ModelSerializationException
import com.adyen.checkout.core.old.internal.data.model.getStringOrNull
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectService
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.old.internal.RedirectHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

@Suppress("TooManyFunctions")
internal class DefaultRedirectDelegate
@Suppress("LongParameterList")
constructor(
    private val observerRepository: ActionObserverRepository,
    override val savedStateHandle: SavedStateHandle,
    override val componentParams: GenericComponentParams,
    private val redirectHandler: RedirectHandler,
    private val paymentDataRepository: PaymentDataRepository,
    private val nativeRedirectService: NativeRedirectService,
    private val analyticsManager: AnalyticsManager?,
) : RedirectDelegate, SavedStateHandleContainer {

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(RedirectComponentViewType)

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private var action: RedirectAction? by SavedStateHandleProperty(ACTION_KEY)

    override fun initialize(coroutineScope: CoroutineScope) {
        _coroutineScope = coroutineScope
        restoreState()
    }

    private fun restoreState() {
        adyenLog(AdyenLogLevel.DEBUG) { "Restoring state" }
        action?.let { initState(it) }
    }

    override fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit
    ) {
        observerRepository.addObservers(
            detailsFlow = detailsFlow,
            exceptionFlow = exceptionFlow,
            permissionFlow = null,
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback,
        )
    }

    override fun removeObserver() {
        observerRepository.removeObservers()
    }

    override fun handleAction(action: Action, activity: Activity) {
        if (action !is RedirectAction) {
            emitError(ComponentException("Unsupported action"))
            return
        }

        this.action = action

        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type.orEmpty(),
        )
        analyticsManager?.trackEvent(event)

        initState(action)
        launchAction(activity, action.url)
    }

    private fun initState(action: RedirectAction) {
        when (action.type) {
            ActionTypes.NATIVE_REDIRECT -> {
                paymentDataRepository.nativeRedirectData = action.nativeRedirectData
            }

            else -> {
                paymentDataRepository.paymentData = action.paymentData
            }
        }
    }

    private fun launchAction(activity: Activity, url: String?) {
        try {
            adyenLog(AdyenLogLevel.DEBUG) { "makeRedirect - $url" }
            // TODO look into emitting a value to tell observers that a redirect was launched so they can track its
            //  status when the app resumes. Currently we have no way of doing that but we can create something like
            //  PaymentComponentState for actions.
            redirectHandler.launchUriRedirect(activity, url)
        } catch (ex: CheckoutException) {
            val event = GenericEvents.error(
                component = action?.paymentMethodType.orEmpty(),
                event = ErrorEvent.REDIRECT_FAILED,
            )
            analyticsManager?.trackEvent(event)

            emitError(ex)
        }
    }

    override fun handleIntent(intent: Intent) {
        try {
            val details = redirectHandler.parseRedirectResult(intent.data)
            val nativeRedirectData = paymentDataRepository.nativeRedirectData
            when {
                action?.type == ActionTypes.NATIVE_REDIRECT -> {
                    handleNativeRedirect(nativeRedirectData, details)
                }

                else -> {
                    emitDetails(details)
                }
            }
        } catch (ex: CheckoutException) {
            val event = GenericEvents.error(
                component = action?.paymentMethodType.orEmpty(),
                event = ErrorEvent.REDIRECT_PARSE_FAILED,
            )
            analyticsManager?.trackEvent(event)

            emitError(ex)
        }
    }

    private fun createActionComponentData(details: JSONObject): ActionComponentData {
        return ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
    }

    private fun handleNativeRedirect(nativeRedirectData: String?, details: JSONObject) {
        coroutineScope.launch {
            val request = NativeRedirectRequest(
                redirectData = nativeRedirectData,
                returnQueryString = details.getStringOrNull(RETURN_URL_QUERY_STRING_PARAMETER).orEmpty(),
            )
            try {
                val response = nativeRedirectService.makeNativeRedirect(request, componentParams.clientKey)
                val detailsJson = NativeRedirectResponse.SERIALIZER.serialize(response)
                emitDetails(detailsJson)
            } catch (e: HttpException) {
                trackNativeRedirectError("Network error")
                emitError(e)
            } catch (e: ModelSerializationException) {
                trackNativeRedirectError("Serialization error")
                emitError(e)
            }
        }
    }

    override fun onError(e: CheckoutException) {
        emitError(e)
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        redirectHandler.setOnRedirectListener(listener)
    }

    private fun trackNativeRedirectError(message: String) {
        val event = GenericEvents.error(
            component = action?.paymentMethodType.orEmpty(),
            event = ErrorEvent.API_NATIVE_REDIRECT,
            message = message,
        )
        analyticsManager?.trackEvent(event)
    }

    private fun emitError(e: CheckoutException) {
        exceptionChannel.trySend(e)
        clearState()
    }

    private fun emitDetails(details: JSONObject) {
        detailsChannel.trySend(createActionComponentData(details))
        clearState()
    }

    private fun clearState() {
        action = null
    }

    override fun onCleared() {
        removeObserver()
        redirectHandler.removeOnRedirectListener()
        _coroutineScope = null
    }

    companion object {
        private const val RETURN_URL_QUERY_STRING_PARAMETER = "returnUrlQueryString"

        @VisibleForTesting
        internal const val ACTION_KEY = "ACTION_KEY"
    }
}
