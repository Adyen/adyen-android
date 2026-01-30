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
import com.adyen.checkout.core.common.exception.ModelSerializationException
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.components.internal.PaymentDataRepository
import com.adyen.checkout.core.components.internal.ui.IntentHandlingComponent
import com.adyen.checkout.core.components.internal.ui.model.ComponentParams
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.error.internal.ComponentError
import com.adyen.checkout.core.error.internal.HttpError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import com.adyen.checkout.core.redirect.internal.ui.RedirectViewEvent
import com.adyen.checkout.core.redirect.internal.ui.redirectEvent
import com.adyen.checkout.redirect.RedirectMainNavigationKey
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectService
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
        RedirectNavKey to CheckoutNavEntry(RedirectNavKey, RedirectMainNavigationKey) { _ -> MainScreen() },
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
        } catch (e: InternalCheckoutError) {
            val event = GenericEvents.error(
                component = action.paymentMethodType.orEmpty(),
                event = ErrorEvent.REDIRECT_PARSE_FAILED,
            )
            analyticsManager.trackEvent(event)

            emitError(e)
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

    private fun launchAction(url: String?) {
        adyenLog(AdyenLogLevel.DEBUG) { "makeRedirect - $url" }
        // TODO look into emitting a value to tell observers that a redirect was launched so they can track its
        //  status when the app resumes. Currently we have no way of doing that but we can create something like
        //  PaymentComponentState for actions.
        redirectEventChannel.trySend(RedirectViewEvent.Redirect(url.orEmpty()))
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
                val detailsJson = NativeRedirectResponse.SERIALIZER.serialize(response)
                emitDetails(detailsJson)
            } catch (e: HttpError) {
                trackNativeRedirectError("Network error")
                emitError(e)
            } catch (e: ModelSerializationException) {
                trackNativeRedirectError("Serialization error")
                // TODO - Error propagation. Fix after ModelSerializationException extends from CheckoutError
                emitError(ComponentError("Serialization error", e))
            }
        }
    }

    private fun emitDetails(details: JSONObject) {
        eventChannel.trySend(
            ActionComponentEvent.ActionDetails(createActionComponentData(details)),
        )
    }

    private fun emitError(error: InternalCheckoutError) {
        val event = GenericEvents.error(
            component = action.paymentMethodType.orEmpty(),
            event = ErrorEvent.REDIRECT_FAILED,
        )
        analyticsManager.trackEvent(event)

        eventChannel.trySend(
            ActionComponentEvent.Error(error),
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
