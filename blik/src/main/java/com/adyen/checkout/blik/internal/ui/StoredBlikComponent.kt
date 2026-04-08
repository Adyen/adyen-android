/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/1/2026.
 */

package com.adyen.checkout.blik.internal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.blik.StoredBlikNavigationKey
import com.adyen.checkout.blik.internal.ui.state.BlikPaymentComponentState
import com.adyen.checkout.blik.internal.ui.state.StoredBlikViewState
import com.adyen.checkout.blik.internal.ui.view.StoredBlikComponent
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.common.internal.helper.bufferedChannel
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPaymentMethod
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import com.adyen.checkout.core.components.internal.ui.navigation.CheckoutNavEntry
import com.adyen.checkout.core.components.paymentmethod.BlikDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

internal class StoredBlikComponent(
    private val storedPaymentMethod: StoredPaymentMethod,
    private val analyticsManager: AnalyticsManager,
    private val sdkDataProvider: SdkDataProvider,
    coroutineScope: CoroutineScope,
) : PaymentComponent<BlikPaymentComponentState> {

    // TODO - Remove navigation
    override val navigation: Map<NavKey, CheckoutNavEntry> = mapOf(
        StoredBlikNavKey to CheckoutNavEntry(StoredBlikNavKey, StoredBlikNavigationKey) { },
    )

    override val navigationStartingPoint: NavKey = StoredBlikNavKey

    private val eventChannel = bufferedChannel<PaymentComponentEvent<BlikPaymentComponentState>>()
    override val eventFlow: Flow<PaymentComponentEvent<BlikPaymentComponentState>> =
        eventChannel.receiveAsFlow()

    private val isLoading = MutableStateFlow(false)

    init {
        initializeAnalytics(coroutineScope)
    }

    private fun initializeAnalytics(coroutineScope: CoroutineScope) {
        analyticsManager.initialize(this, coroutineScope)
    }

    override fun submit() {
        val paymentComponentState = createPaymentComponentState()
        eventChannel.trySend(PaymentComponentEvent.Submit(paymentComponentState))
    }

    override fun setLoading(isLoading: Boolean) {
        this.isLoading.value = isLoading
    }

    override fun onCleared() {
        analyticsManager.clear(this)
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

    @Composable
    override fun Content(modifier: Modifier) {
        val isLoading by this.isLoading.collectAsStateWithLifecycle()

        StoredBlikComponent(
            viewState = StoredBlikViewState(isLoading = isLoading),
            onSubmitClick = ::submit,
        )
    }
}
