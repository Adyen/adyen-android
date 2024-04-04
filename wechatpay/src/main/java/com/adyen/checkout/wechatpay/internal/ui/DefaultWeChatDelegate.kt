/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2022.
 */

package com.adyen.checkout.wechatpay.internal.ui

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.WeChatPaySdkData
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.analytics.AnalyticsManager
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.wechatpay.internal.util.WeChatRequestGenerator
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONException
import org.json.JSONObject

@Suppress("TooManyFunctions")
internal class DefaultWeChatDelegate(
    private val observerRepository: ActionObserverRepository,
    override val componentParams: GenericComponentParams,
    private val iwxApi: IWXAPI,
    private val payRequestGenerator: WeChatRequestGenerator<*>,
    private val paymentDataRepository: PaymentDataRepository,
    private val analyticsManager: AnalyticsManager?,
) : WeChatDelegate {

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(WeChatComponentViewType)

    override fun initialize(coroutineScope: CoroutineScope) {
        // no ops
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

    private val eventHandler = object : IWXAPIEventHandler {
        override fun onReq(baseReq: BaseReq) = Unit

        override fun onResp(baseResp: BaseResp) {
            onResponse(baseResp)
        }
    }

    @VisibleForTesting
    internal fun onResponse(baseResponse: BaseResp) {
        parseResult(baseResponse)?.let { response ->
            detailsChannel.trySend(createActionComponentData(response))
        }
    }

    private fun parseResult(baseResp: BaseResp): JSONObject? {
        val result = JSONObject()
        try {
            result.put(RESULT_CODE, baseResp.errCode)
        } catch (e: JSONException) {
            exceptionChannel.trySend(CheckoutException("Error parsing result.", e))
            return null
        }
        return result
    }

    override fun handleIntent(intent: Intent) {
        iwxApi.handleIntent(intent, eventHandler)
    }

    @SuppressWarnings("ReturnCount")
    override fun handleAction(action: Action, activity: Activity) {
        @Suppress("UNCHECKED_CAST")
        val sdkAction = (action as? SdkAction<WeChatPaySdkData>)
        if (sdkAction == null) {
            exceptionChannel.trySend(ComponentException("Unsupported action"))
            return
        }

        val activityName = activity.javaClass.name
        adyenLog(AdyenLogLevel.DEBUG) { "handleAction: activity - $activityName" }

        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Payment data is null" }
            exceptionChannel.trySend(ComponentException("Payment data is null"))
            return
        }

        val sdkData = action.sdkData
        if (sdkData == null) {
            exceptionChannel.trySend(ComponentException("SDK Data is null"))
            return
        }

        val event = GenericEvents.action(
            component = action.paymentMethodType.orEmpty(),
            subType = action.type.orEmpty(),
        )
        analyticsManager?.trackEvent(event)

        val isWeChatNotInitiated = !initiateWeChatPayRedirect(sdkData, activityName)

        if (isWeChatNotInitiated) {
            exceptionChannel.trySend(ComponentException("Failed to initialize WeChat app"))
            return
        }
    }

    private fun initiateWeChatPayRedirect(weChatPaySdkData: WeChatPaySdkData, activityName: String): Boolean {
        adyenLog(AdyenLogLevel.DEBUG) { "initiateWeChatPayRedirect" }
        iwxApi.registerApp(weChatPaySdkData.appid)
        val request = payRequestGenerator.generate(weChatPaySdkData, activityName)
        return iwxApi.sendReq(request)
    }

    private fun createActionComponentData(details: JSONObject): ActionComponentData {
        return ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
    }

    override fun onError(e: CheckoutException) {
        exceptionChannel.trySend(e)
    }

    override fun onCleared() {
        removeObserver()
    }

    companion object {
        private const val RESULT_CODE = "resultCode"
    }
}
