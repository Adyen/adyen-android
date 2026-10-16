/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 24/10/2025.
 */

package com.adyen.checkout.redirect.internal.ui

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.ActionTypes
import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.action.internal.RestorableActionComponent
import com.adyen.checkout.core.action.internal.ReturningActionComponent
import com.adyen.checkout.core.action.internal.ui.ActionFormatter
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.common.internal.model.getStringOrNull
import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.InternalCheckoutError
import com.adyen.checkout.core.redirect.internal.RedirectHandler
import com.adyen.checkout.core.redirect.internal.ui.RedirectViewEvent
import com.adyen.checkout.core.redirect.internal.ui.redirectEvent
import com.adyen.checkout.redirect.internal.data.api.NativeRedirectRepository
import com.adyen.checkout.redirect.internal.ui.view.RedirectContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

@Suppress("TooManyFunctions")
internal class RedirectComponent
@Suppress("LongParameterList")
constructor(
    private val action: RedirectAction,
    private val analyticsManager: AnalyticsManager,
    private val redirectHandler: RedirectHandler,
    private val nativeRedirectRepository: NativeRedirectRepository,
    private val clientKey: String,
    private val coroutineScope: CoroutineScope,
) : ActionComponent, ReturningActionComponent, RestorableActionComponent {

    private val eventChannel = bufferedChannel<ActionComponentEvent>()
    override val eventFlow: Flow<ActionComponentEvent> = eventChannel.receiveAsFlow()

    private val redirectEventChannel = bufferedChannel<RedirectViewEvent>()

    @VisibleForTesting
    internal val redirectEventFlow: Flow<RedirectViewEvent> = redirectEventChannel.receiveAsFlow()

    @Volatile
    private var paymentData: String? = null

    @Volatile
    private var nativeRedirectData: String? = null

    @Composable
    override fun Content(modifier: Modifier) {
        redirectEvent(
            redirectHandler = redirectHandler,
            viewEventFlow = redirectEventFlow,
            onError = ::emitError,
        )

        RedirectContent(
            logoTxVariant = ActionFormatter.getIcon(action),
            modifier = modifier,
        )
    }

    override fun handleAction() {
        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type,
        )
        analyticsManager.trackEvent(event)

        initState()
        launchAction(action.url)
    }

    /**
     * The shopper already left for the redirect before process death, so nothing is launched again. The component waits
     * for [handleReturn].
     */
    override fun restoreAction() {
        initState()
    }

    private fun initState() {
        when (action.type) {
            ActionTypes.NATIVE_REDIRECT -> {
                nativeRedirectData = action.nativeRedirectData
            }

            else -> {
                paymentData = action.paymentData
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

    override fun handleReturn(intent: Intent) {
        adyenLog(AdyenLogLevel.DEBUG) { "redirect component handle intent" }
        try {
            val details = redirectHandler.parseRedirectResult(intent.data)
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

    private fun handleNativeRedirect(nativeRedirectData: String?, details: JSONObject) {
        coroutineScope.launch {
            nativeRedirectRepository.makeNativeRedirect(
                nativeRedirectData = nativeRedirectData,
                returnQueryString = details.getStringOrNull(RETURN_URL_QUERY_STRING_PARAMETER).orEmpty(),
                clientKey = clientKey,
            ).fold(
                onSuccess = ::emitDetails,
                onFailure = { e ->
                    trackNativeRedirectError(if (e is JSONException) "Serialization error" else "Network error")
                    emitError(e as? InternalCheckoutError ?: GenericError("Native redirect request failed.", e))
                },
            )
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
            paymentData = paymentData,
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
