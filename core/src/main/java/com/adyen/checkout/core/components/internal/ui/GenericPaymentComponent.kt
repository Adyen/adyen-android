/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/6/2026.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.state.GenericPaymentComponentState
import com.adyen.checkout.core.components.paymentmethod.GenericDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class GenericPaymentComponent(
    private val analyticsManager: AnalyticsManager,
    private val paymentMethodType: String,
    private val sdkDataProvider: SdkDataProvider,
) : PaymentComponent {

    private val eventChannel = bufferedChannel<PaymentComponentEvent>()
    override val eventFlow: Flow<PaymentComponentEvent> = eventChannel.receiveAsFlow()

    init {
        initializeAnalytics()
    }

    private fun initializeAnalytics() {
        analyticsManager.initialize(this)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        // This component has no UI
    }

    override fun submit() {
        val paymentComponentState = GenericPaymentComponentState(
            data = PaymentComponentData(
                paymentMethod = GenericDetails(
                    type = paymentMethodType,
                    sdkData = sdkDataProvider.createEncodedSdkData(),
                    // TODO - Check if we should remove subtype from GenericDetails
                    subtype = null,
                ),
                order = null,
            ),
            isValid = true,
        )

        eventChannel.trySend(
            PaymentComponentEvent.Submit(paymentComponentState),
        )
    }

    override fun requiresUserInteraction(): Boolean = false

    override fun setLoading(isLoading: Boolean) {
        // There is no UI to display a loading state
    }

    override fun onCleared() {
        analyticsManager.clear(this)
    }
}
