/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 20/10/2023.
 */

package com.adyen.checkout.twint.internal.ui

import android.app.Activity
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.TwintSdkData
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionObserverRepository
import com.adyen.checkout.components.core.internal.PaymentDataRepository
import com.adyen.checkout.components.core.internal.ui.model.GenericComponentParams
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.twint.Twint
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONObject

internal class DefaultTwintDelegate(
    private val observerRepository: ActionObserverRepository,
    override val componentParams: GenericComponentParams,
    private val paymentDataRepository: PaymentDataRepository,
) : TwintDelegate {

    private val detailsChannel: Channel<ActionComponentData> = bufferedChannel()
    override val detailsFlow: Flow<ActionComponentData> = detailsChannel.receiveAsFlow()

    private val exceptionChannel: Channel<CheckoutException> = bufferedChannel()
    override val exceptionFlow: Flow<CheckoutException> = exceptionChannel.receiveAsFlow()

    override val viewFlow: Flow<ComponentViewType?> = MutableStateFlow(TwintComponentViewType)

    override fun initialize(coroutineScope: CoroutineScope) = Unit

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

    @SuppressWarnings("ReturnCount")
    override fun handleAction(action: Action, activity: Activity) {
        val sdkAction = action as? SdkAction<*>
        if (sdkAction == null) {
            exceptionChannel.trySend(ComponentException("Unsupported action"))
            return
        }

        val paymentData = action.paymentData
        paymentDataRepository.paymentData = paymentData
        if (paymentData == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Payment data is null" }
            exceptionChannel.trySend(ComponentException("Payment data is null"))
            return
        }

        val sdkData = sdkAction.sdkData
        if (sdkData == null || sdkData !is TwintSdkData) {
            exceptionChannel.trySend(ComponentException("SDK Data is null or of wrong type"))
            return
        }

        Twint.setResultListener(::handleTwintResult)
        try {
            Twint.payWithCode(sdkData.token)
        } catch (e: CheckoutException) {
            exceptionChannel.trySend(e)
        }
    }

    @VisibleForTesting
    internal fun handleTwintResult(result: TwintPayResult) {
        when (result) {
            TwintPayResult.TW_B_SUCCESS -> {
                detailsChannel.trySend(createActionComponentData())
            }

            TwintPayResult.TW_B_ERROR -> {
                onError(ComponentException("Twint encountered an error."))
            }

            TwintPayResult.TW_B_APP_NOT_INSTALLED -> {
                onError(ComponentException("Twint app not installed."))
            }
        }
    }

    private fun createActionComponentData(): ActionComponentData {
        return ActionComponentData(
            // The backend doesn't accept null, so we have to send an empty json object.
            details = JSONObject(),
            paymentData = paymentDataRepository.paymentData,
        )
    }

    override fun onError(e: CheckoutException) {
        exceptionChannel.trySend(e)
    }

    override fun onCleared() {
        removeObserver()
    }
}
