/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/1/2026.
 */

package com.adyen.checkout.blik.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.blik.internal.ui.state.BlikPaymentComponentState
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.paymentmethod.BlikDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class StoredBlikComponent(
    private val storedPaymentMethod: StoredPaymentMethod,
    private val analyticsManager: AnalyticsManager,
    private val sdkDataProvider: SdkDataProvider,
) : PaymentComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    init {
        trackRenderEvent()
    }

    private fun trackRenderEvent() {
        val event = GenericEvents.rendered(
            component = storedPaymentMethod.type,
            isStoredPaymentMethod = true,
        )
        analyticsManager.trackEvent(event)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        // This component has no UI
    }

    override fun submit() {
        val paymentComponentState = createPaymentComponentState()
        eventChannel.trySend(PaymentComponentEvent.Submit(paymentComponentState))
    }

    private fun createPaymentComponentState(): BlikPaymentComponentState {
        val blikDetails = BlikDetails(
            type = BlikDetails.PAYMENT_METHOD_TYPE,
            blikCode = null,
            storedPaymentMethodId = storedPaymentMethod.id,
            sdkData = sdkDataProvider.createEncodedSdkData(),
        )

        val paymentComponentData = PaymentComponentData(
            paymentMethod = blikDetails,
            order = null,
        )

        return BlikPaymentComponentState(
            data = paymentComponentData,
            isValid = true,
        )
    }

    override fun requiresUserInteraction(): Boolean = false

    override fun setLoading(isLoading: Boolean) = Unit

    override fun onCleared() = Unit
}
