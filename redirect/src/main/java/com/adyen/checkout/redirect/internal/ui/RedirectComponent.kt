/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/10/2025.
 */

package com.adyen.checkout.redirect.internal.ui

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.ActionTypes
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.exception.HttpException
import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.components.ComponentError
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.ui.IntentHandlingComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import com.adyen.checkout.core.redirect.internal.ui.RedirectViewEvent
import com.adyen.checkout.core.redirect.internal.ui.redirectEvent
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectService
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.json.JSONObject

internal class RedirectComponent(
    private val action: RedirectAction,
    private val analyticsManager: AnalyticsManager,
    private val redirectHandler: RedirectHandler,
    private val paymentDataRepository: PaymentDataRepository,
    private val nativeRedirectService: NativeRedirectService,
    private val componentParams: ComponentParams,
) : ActionComponent, IntentHandlingComponent {

    private var _coroutineScope: CoroutineScope? = null
    private val coroutineScope: CoroutineScope get() = requireNotNull(_coroutineScope)

    private val eventChannel = bufferedChannel<ActionComponentEvent>()
    override val eventFlow: Flow<ActionComponentEvent> = eventChannel.receiveAsFlow()

    private val redirectEventChannel = bufferedChannel<RedirectViewEvent>()
    private val redirectEventFlow: Flow<RedirectViewEvent> = redirectEventChannel.receiveAsFlow()

    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        RedirectNavKey to CheckoutNavEntry(RedirectNavKey) { _ -> MainScreen() },
    )

    override val navigationStartingPoint: NavKey = RedirectNavKey

    @Composable
    private fun MainScreen() {
        redirectEvent(
            redirectHandler = redirectHandler,
            viewEventFlow = redirectEventFlow,
            onError = ::emitError,
        )
    }

    override fun handleAction() {
        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type.orEmpty(),
        )
        analyticsManager.trackEvent(event)

        initState()
        launchAction(action.url)
    }

    @Suppress("TooGenericExceptionCaught")
    override fun handleIntent(intent: Intent) {
        adyenLog(AdyenLogLevel.DEBUG) { "redirect component handle intent" }
        try {
            val details = redirectHandler.parseRedirectResult(intent.data)
            val nativeRedirectData = paymentDataRepository.nativeRedirectData
            when {
                action.type == ActionTypes.NATIVE_REDIRECT -> {
                    handleNativeRedirect(nativeRedirectData, details)
                }

                else -> {
                    emitDetails(details)
                }
            }
        } catch (ex: RuntimeException) {
            // TODO - Error propagation
            val event = GenericEvents.error(
                component = action.paymentMethodType.orEmpty(),
                event = ErrorEvent.REDIRECT_PARSE_FAILED,
            )
            analyticsManager.trackEvent(event)

            emitError(ex)
        }
    }

    private fun initState() {
        when (action.type) {
            ActionTypes.NATIVE_REDIRECT -> {
                paymentDataRepository.nativeRedirectData = action.nativeRedirectData
            }

            else -> {
                paymentDataRepository.paymentData = action.paymentData
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun launchAction(url: String?) {
        try {
            adyenLog(AdyenLogLevel.DEBUG) { "makeRedirect - $url" }
            // TODO look into emitting a value to tell observers that a redirect was launched so they can track its
            //  status when the app resumes. Currently we have no way of doing that but we can create something like
            //  PaymentComponentState for actions.
            redirectEventChannel.trySend(RedirectViewEvent.Redirect(url.orEmpty()))
        } catch (ex: RuntimeException) {
            // TODO - Error propagation
            val event = GenericEvents.error(
                component = action.paymentMethodType.orEmpty(),
                event = ErrorEvent.REDIRECT_FAILED,
            )
            analyticsManager.trackEvent(event)

            emitError(ex)
        }
    }

    private fun handleNativeRedirect(nativeRedirectData: String?, details: JSONObject) {
        coroutineScope.launch {
            val request = NativeRedirectRequest(
                redirectData = nativeRedirectData,
                returnQueryString = details.getStringOrNull(RETURN_URL_QUERY_STRING_PARAMETER)
                    .orEmpty(),
            )
            try {
                val response = nativeRedirectService.makeNativeRedirect(request, componentParams.clientKey)
                val detailsJson = NativeRedirectResponse.Companion.SERIALIZER.serialize(response)
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

    private fun emitDetails(details: JSONObject) {
        eventChannel.trySend(
            ActionComponentEvent.ActionDetails(createActionComponentData(details)),
        )
    }

    // TODO - Error propagation
    private fun emitError(e: RuntimeException) {
        eventChannel.trySend(
            ActionComponentEvent.Error(ComponentError(e)),
        )
    }

    private fun createActionComponentData(details: JSONObject): ActionComponentData {
        return ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
    }

    private fun trackNativeRedirectError(message: String) {
        val event = GenericEvents.error(
            component = action.paymentMethodType.orEmpty(),
            event = ErrorEvent.API_NATIVE_REDIRECT,
            message = message,
        )
        analyticsManager.trackEvent(event)
    }

    companion object {
        private const val RETURN_URL_QUERY_STRING_PARAMETER = "returnUrlQueryString"
    }
}

@Serializable
private data object RedirectNavKey : NavKey
